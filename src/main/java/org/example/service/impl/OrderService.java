package org.example.service.impl;

import org.example.entity.Order;
import org.example.entity.Station;
import org.example.entity.Vehicle;
import org.example.repository.OrderRepository;
import org.example.repository.StationRepository;
import org.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private int orderCounter = 1;

    @Transactional
    public Order create(Order order) {
        // 验证起始和终止站点
        Station origin = stationRepository.findById(order.getOriginStationId())
                .orElseThrow(() -> new IllegalArgumentException("起始站点不存在: " + order.getOriginStationId()));
        Station dest = stationRepository.findById(order.getDestinationStationId())
                .orElseThrow(() -> new IllegalArgumentException("终止站点不存在: " + order.getDestinationStationId()));

        if (order.getOriginStationId().equals(order.getDestinationStationId())) {
            throw new IllegalArgumentException("起始地点和终止地点不能相同");
        }

        // 生成订单ID
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(generateOrderId(order.getStartTime()));
        }

        // 计算预计到达时间和收益
        if (order.getEstimatedArrivalTime() == null) {
            int durationMinutes = calculateEstimatedDuration(origin, dest);
            order.setEstimatedArrivalTime(order.getStartTime().plusMinutes(durationMinutes));
        }
        if (order.getEstimatedRevenue() == null) {
            order.setEstimatedRevenue(calculateRevenue(origin, dest));
        }

        // 默认状态
        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.待派单);
        }

        return orderRepository.save(order);
    }

    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findByVehicle(String vehicleId) {
        return orderRepository.findByVehicleId(vehicleId);
    }

    @Transactional
    public Order assignVehicle(String orderId, String vehicleId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + vehicleId));

        if (vehicle.getStatus() != Vehicle.VehicleStatus.巡游中) {
            throw new IllegalArgumentException("车辆状态不是巡游中，无法派单");
        }

        order.setVehicleId(vehicleId);
        order.setStatus(Order.OrderStatus.进行中);
        vehicle.setStatus(Vehicle.VehicleStatus.履约中);
        vehicle.setOrderId(orderId);

        vehicleRepository.save(vehicle);
        return orderRepository.save(order);
    }

    @Transactional
    public Order complete(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        if (order.getVehicleId() == null) {
            throw new IllegalArgumentException("订单未派单");
        }

        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + order.getVehicleId()));

        order.setStatus(Order.OrderStatus.已完成);
        order.setActualArrivalTime(LocalDateTime.now());
        
        // 重置车辆状态为巡游中
        vehicle.setStatus(Vehicle.VehicleStatus.巡游中);
        vehicle.setOrderId(null);
        
        // 清理派单相关字段
        vehicle.setPickupPath(null);
        vehicle.setPickupProgress(null);
        vehicle.setDeliveryPath(null);
        vehicle.setDeliveryProgress(null);
        vehicle.setDispatchOrderStartTime(null);

        vehicleRepository.save(vehicle);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancel(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        if (order.getStatus() == Order.OrderStatus.已完成) {
            throw new IllegalArgumentException("订单已完成，无法取消");
        }

        if (order.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
                    .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + order.getVehicleId()));
            
            // 重置车辆状态为巡游中
            vehicle.setStatus(Vehicle.VehicleStatus.巡游中);
            vehicle.setOrderId(null);
            
            // 清理派单相关字段
            vehicle.setPickupPath(null);
            vehicle.setPickupProgress(null);
            vehicle.setDeliveryPath(null);
            vehicle.setDeliveryProgress(null);
            vehicle.setDispatchOrderStartTime(null);
            
            vehicleRepository.save(vehicle);
        }

        order.setStatus(Order.OrderStatus.已取消);
        return orderRepository.save(order);
    }

    private String generateOrderId(LocalDateTime startTime) {
        String timeStr = startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return String.format("nj-%s-%04d", timeStr, orderCounter++);
    }

    private int calculateEstimatedDuration(Station origin, Station dest) {
        double distance = calculateDistance(origin.getLatitude(), origin.getLongitude(),
                dest.getLatitude(), dest.getLongitude());
        // 短途10-30分钟，中途30-60分钟，长途60-120分钟
        if (distance < 5) return (int) (10 + Math.random() * 20);
        else if (distance < 15) return (int) (30 + Math.random() * 30);
        else return (int) (60 + Math.random() * 60);
    }

    private Double calculateRevenue(Station origin, Station dest) {
        double distance = calculateDistance(origin.getLatitude(), origin.getLongitude(),
                dest.getLatitude(), dest.getLongitude());
        // 短途5-20元，中途20-50元，长途50-120元
        if (distance < 5) return 5 + Math.random() * 15;
        else if (distance < 15) return 20 + Math.random() * 30;
        else return 50 + Math.random() * 70;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * 批量模拟创建订单（公共方法，供其他服务调用）
     * @param count 订单数量
     * @return 实际创建的订单数量
     */
    @Transactional
    public int simulateOrders(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("订单数量必须大于0");
        }

        List<Station> stations = stationRepository.findAll();
        if (stations.size() < 2) {
            throw new IllegalArgumentException("站点数量不足，至少需要2个站点");
        }

        int created = 0;
        for (int i = 0; i < count; i++) {
            try {
                // 随机选择起点和终点（不能相同）
                Station origin, destination;
                do {
                    origin = stations.get((int) (Math.random() * stations.size()));
                    destination = stations.get((int) (Math.random() * stations.size()));
                } while (origin.getId().equals(destination.getId()));

                // 创建订单
                Order order = Order.builder()
                        .originStationId(origin.getId())
                        .destinationStationId(destination.getId())
                        .startTime(LocalDateTime.now())
                        .status(Order.OrderStatus.待派单)
                        .build();

                // 计算预计到达时间和收益
                int durationMinutes = calculateEstimatedDuration(origin, destination);
                order.setEstimatedArrivalTime(order.getStartTime().plusMinutes(durationMinutes));
                order.setEstimatedRevenue(calculateRevenue(origin, destination));

                // 生成订单ID
                order.setId(generateOrderId(order.getStartTime()));

                orderRepository.save(order);
                created++;
            } catch (Exception e) {
                log.error("创建模拟订单失败", e);
            }
        }

        log.info("成功创建{}个模拟订单", created);
        return created;
    }
}
