package org.example.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.OrderDispatchConfig;
import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;
import org.example.entity.Order;
import org.example.entity.Station;
import org.example.entity.Vehicle;
import org.example.repository.OrderRepository;
import org.example.repository.StationRepository;
import org.example.repository.VehicleRepository;
import org.example.service.RouteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单派单服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDispatchService {

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final StationRepository stationRepository;
    private final RouteService routeService;
    private final OrderDispatchConfig config;
    private final ObjectMapper objectMapper;

    /**
     * 定时派单任务，每2分钟执行一次
     */
    @Scheduled(fixedDelayString = "${order-dispatch.interval-ms:120000}")
    public void dispatchOrders() {
        if (!config.isEnabled()) {
            return;
        }

        log.info("开始执行订单派单任务...");
        
        // 查找所有"巡游中"状态的车辆
        List<Vehicle> cruisingVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.巡游中);
        
        // 查找所有"待派单"状态的订单
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.待派单);
        
        if (cruisingVehicles.isEmpty() || pendingOrders.isEmpty()) {
            log.info("没有可派的车辆或订单");
            return;
        }
        
        // 随机打乱顺序
        Collections.shuffle(cruisingVehicles);
        Collections.shuffle(pendingOrders);
        
        int dispatchCount = 0;
        int minSize = Math.min(cruisingVehicles.size(), pendingOrders.size());
        
        for (int i = 0; i < minSize; i++) {
            Vehicle vehicle = cruisingVehicles.get(i);
            Order order = pendingOrders.get(i);
            
            try {
                dispatchOrder(vehicle, order);
                dispatchCount++;
            } catch (Exception e) {
                log.error("派单失败: 车辆={}, 订单={}", vehicle.getId(), order.getId(), e);
            }
        }
        
        log.info("订单派单完成，成功派单{}单", dispatchCount);
    }

    /**
     * 派单：将车辆与订单匹配
     */
    @Transactional
    public void dispatchOrder(Vehicle vehicle, Order order) {
        // 获取订单起点和终点站点
        Station originStation = stationRepository.findById(order.getOriginStationId())
                .orElseThrow(() -> new IllegalArgumentException("起点站点不存在: " + order.getOriginStationId()));
        Station destStation = stationRepository.findById(order.getDestinationStationId())
                .orElseThrow(() -> new IllegalArgumentException("终点站点不存在: " + order.getDestinationStationId()));
        
        // 规划接乘路线（车辆当前位置 -> 订单起点）
        RouteRequest pickupRequest = new RouteRequest();
        pickupRequest.setOrigin(vehicle.getLatitude() + "," + vehicle.getLongitude());
        pickupRequest.setDestination(originStation.getLatitude() + "," + originStation.getLongitude());
        pickupRequest.setCoordinateMode(true);
        RouteResponse pickupResponse = routeService.planRoute(pickupRequest);
        
        // 处理路径数据
        List<double[]> pickupPath = new ArrayList<>();
        if (pickupResponse.getPath() instanceof List) {
            pickupPath = (List<double[]>) pickupResponse.getPath();
        }
        
        // 规划履约路线（订单起点 -> 订单终点）
        RouteRequest deliveryRequest = new RouteRequest();
        deliveryRequest.setOrigin(originStation.getLatitude() + "," + originStation.getLongitude());
        deliveryRequest.setDestination(destStation.getLatitude() + "," + destStation.getLongitude());
        deliveryRequest.setCoordinateMode(true);
        RouteResponse deliveryResponse = routeService.planRoute(deliveryRequest);
        
        // 处理路径数据
        List<double[]> deliveryPath = new ArrayList<>();
        if (deliveryResponse.getPath() instanceof List) {
            deliveryPath = (List<double[]>) deliveryResponse.getPath();
        }
        
        try {
            // 更新车辆状态
            vehicle.setStatus(Vehicle.VehicleStatus.接乘中);
            vehicle.setOrderId(order.getId());
            vehicle.setPickupPath(objectMapper.writeValueAsString(pickupPath));
            vehicle.setPickupProgress(0);
            vehicle.setDeliveryPath(objectMapper.writeValueAsString(deliveryPath));
            vehicle.setDeliveryProgress(0);
            vehicle.setDispatchOrderStartTime(LocalDateTime.now());
            vehicleRepository.save(vehicle);
            
            // 更新订单状态
            order.setVehicleId(vehicle.getId());
            order.setStatus(Order.OrderStatus.待接乘);
            order.setPickupStartTime(LocalDateTime.now());
            orderRepository.save(order);
            
            log.info("派单成功: 车辆{} -> 订单{}, 接乘路线{}个点, 履约路线{}个点", 
                    vehicle.getId(), order.getId(), pickupPath.size(), deliveryPath.size());
        } catch (Exception e) {
            throw new RuntimeException("派单失败", e);
        }
    }

    /**
     * 手动触发派单
     */
    public String triggerDispatch() {
        dispatchOrders();
        return "派单任务已触发";
    }

    /**
     * 模拟创建订单并派单
     */
    @Transactional
    public int simulateAndDispatch(int orderCount) {
        // 先创建模拟订单
        OrderService orderService = new OrderService(orderRepository, stationRepository, vehicleRepository);
        int created = orderService.simulateOrders(orderCount);
        
        // 然后执行派单
        dispatchOrders();
        
        return created;
    }

    /**
     * 获取派单中的车辆信息
     */
    public List<Map<String, Object>> getDispatchingVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatusIn(List.of(
                Vehicle.VehicleStatus.接乘中,
                Vehicle.VehicleStatus.履约中
        ));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Vehicle v : vehicles) {
            Map<String, Object> info = new HashMap<>();
            info.put("vehicleId", v.getId());
            info.put("vin", v.getVin());
            info.put("status", v.getStatus().name());
            info.put("orderId", v.getOrderId() != null ? v.getOrderId() : "");
            info.put("latitude", v.getLatitude());
            info.put("longitude", v.getLongitude());
            
            // 添加路线信息
            if (v.getStatus() == Vehicle.VehicleStatus.接乘中) {
                info.put("pickupProgress", v.getPickupProgress() != null ? v.getPickupProgress() : 0);
                try {
                    if (v.getPickupPath() != null) {
                        List<double[]> path = objectMapper.readValue(v.getPickupPath(), new TypeReference<List<double[]>>() {});
                        info.put("pickupPath", path);
                    }
                } catch (Exception e) {
                    log.error("解析接乘路线失败", e);
                }
            } else if (v.getStatus() == Vehicle.VehicleStatus.履约中) {
                info.put("deliveryProgress", v.getDeliveryProgress() != null ? v.getDeliveryProgress() : 0);
                try {
                    if (v.getDeliveryPath() != null) {
                        List<double[]> path = objectMapper.readValue(v.getDeliveryPath(), new TypeReference<List<double[]>>() {});
                        info.put("deliveryPath", path);
                    }
                } catch (Exception e) {
                    log.error("解析履约路线失败", e);
                }
            }
            
            // 添加订单信息
            if (v.getOrderId() != null) {
                orderRepository.findById(v.getOrderId()).ifPresent(order -> {
                    info.put("orderStatus", order.getStatus().name());
                    info.put("originStationId", order.getOriginStationId());
                    info.put("destStationId", order.getDestinationStationId());
                    
                    stationRepository.findById(order.getOriginStationId()).ifPresent(station -> {
                        info.put("originLatitude", station.getLatitude());
                        info.put("originLongitude", station.getLongitude());
                        info.put("originStationName", station.getName());
                    });
                    
                    stationRepository.findById(order.getDestinationStationId()).ifPresent(station -> {
                        info.put("destLatitude", station.getLatitude());
                        info.put("destLongitude", station.getLongitude());
                        info.put("destStationName", station.getName());
                    });
                });
            }
            
            result.add(info);
        }
        
        return result;
    }

    /**
     * 车辆到达接乘点（起点）
     */
    @Transactional
    public void vehicleArrivedPickup(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + vehicleId));
        
        if (vehicle.getStatus() != Vehicle.VehicleStatus.接乘中) {
            return;
        }
        
        Order order = orderRepository.findById(vehicle.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + vehicle.getOrderId()));
        
        // 更新车辆状态为履约中
        vehicle.setStatus(Vehicle.VehicleStatus.履约中);
        vehicle.setPickupProgress(100);
        vehicle.setDeliveryProgress(0);
        
        // 更新订单状态为进行中
        order.setStatus(Order.OrderStatus.进行中);
        order.setPickupArrivalTime(LocalDateTime.now());
        
        // 更新车辆位置到起点
        stationRepository.findById(order.getOriginStationId()).ifPresent(station -> {
            vehicle.setLatitude(station.getLatitude());
            vehicle.setLongitude(station.getLongitude());
        });
        
        vehicleRepository.save(vehicle);
        orderRepository.save(order);
        
        log.info("车辆{}已到达接乘点，开始履约订单{}", vehicleId, order.getId());
    }

    /**
     * 车辆到达目的地（终点）
     */
    @Transactional
    public void vehicleArrivedDestination(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + vehicleId));
        
        if (vehicle.getStatus() != Vehicle.VehicleStatus.履约中) {
            return;
        }
        
        Order order = orderRepository.findById(vehicle.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + vehicle.getOrderId()));
        
        // 更新车辆状态为巡游中
        vehicle.setStatus(Vehicle.VehicleStatus.巡游中);
        vehicle.setOrderId(null);
        vehicle.setPickupPath(null);
        vehicle.setPickupProgress(null);
        vehicle.setDeliveryPath(null);
        vehicle.setDeliveryProgress(null);
        vehicle.setDispatchOrderStartTime(null);
        
        // 更新订单状态为已完成
        order.setStatus(Order.OrderStatus.已完成);
        order.setActualArrivalTime(LocalDateTime.now());
        
        // 更新车辆位置到终点
        stationRepository.findById(order.getDestinationStationId()).ifPresent(station -> {
            vehicle.setLatitude(station.getLatitude());
            vehicle.setLongitude(station.getLongitude());
        });
        
        vehicleRepository.save(vehicle);
        orderRepository.save(order);
        
        log.info("车辆{}已完成订单{}", vehicleId, order.getId());
    }
}
