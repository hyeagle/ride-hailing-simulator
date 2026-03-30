package org.example.service;

import org.example.entity.Vehicle;
import org.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private int idCounter = 1;
    private final Random random = new Random();

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        // 验证VIN码
        validateVin(vehicle.getVin());

        // 验证电量
        if (vehicle.getBatteryPercent() < 0 || vehicle.getBatteryPercent() > 100) {
            throw new IllegalArgumentException("电量百分比必须在0-100之间");
        }

        // 验证经纬度
        validateNanjingCoordinates(vehicle.getLatitude(), vehicle.getLongitude());

        // 生成ID和GIS网格
        if (vehicle.getId() == null || vehicle.getId().isEmpty()) {
            vehicle.setId(String.format("C%04d", idCounter++));
        }
        if (vehicle.getGisGrid() == null) {
            vehicle.setGisGrid(calculateGisGrid(vehicle.getLatitude(), vehicle.getLongitude()));
        }

        // 履约中必须有订单ID
        if (vehicle.getStatus() == Vehicle.VehicleStatus.履约中 && 
            (vehicle.getOrderId() == null || vehicle.getOrderId().isEmpty())) {
            throw new IllegalArgumentException("履约中状态必须关联订单ID");
        }

        return vehicleRepository.save(vehicle);
    }

    public Optional<Vehicle> findById(String id) {
        return vehicleRepository.findById(id);
    }

    public Optional<Vehicle> findByVin(String vin) {
        return vehicleRepository.findByVin(vin);
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> findByStatus(Vehicle.VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }

    public List<Vehicle> findByBatteryRange(Integer min, Integer max) {
        return vehicleRepository.findByBatteryPercentBetween(min, max);
    }

    public List<Vehicle> findByGisGrid(String gisGrid) {
        return vehicleRepository.findByGisGrid(gisGrid);
    }

    @Transactional
    public Vehicle update(String id, Vehicle updated) {
        return vehicleRepository.findById(id).map(existing -> {
            if (updated.getStatus() != null) {
                existing.setStatus(updated.getStatus());
                if (updated.getStatus() != Vehicle.VehicleStatus.履约中) {
                    existing.setOrderId(null);
                }
            }
            if (updated.getOrderId() != null) {
                if (existing.getStatus() != Vehicle.VehicleStatus.履约中) {
                    throw new IllegalArgumentException("非履约中状态不能关联订单ID");
                }
                existing.setOrderId(updated.getOrderId());
            }
            if (updated.getBatteryPercent() != null) {
                if (updated.getBatteryPercent() < 0 || updated.getBatteryPercent() > 100) {
                    throw new IllegalArgumentException("电量百分比必须在0-100之间");
                }
                existing.setBatteryPercent(updated.getBatteryPercent());
            }
            if (updated.getLatitude() != null && updated.getLongitude() != null) {
                validateNanjingCoordinates(updated.getLatitude(), updated.getLongitude());
                existing.setLatitude(updated.getLatitude());
                existing.setLongitude(updated.getLongitude());
                existing.setGisGrid(calculateGisGrid(updated.getLatitude(), updated.getLongitude()));
            }
            return vehicleRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + id));
    }

    @Transactional
    public void delete(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + id));
        if (vehicle.getStatus() == Vehicle.VehicleStatus.履约中) {
            throw new IllegalArgumentException("车辆正在履约，无法删除");
        }
        vehicleRepository.deleteById(id);
    }

    private void validateVin(String vin) {
        if (vin == null || vin.length() != 17) {
            throw new IllegalArgumentException("VIN码必须为17位");
        }
        if (vin.contains("I") || vin.contains("O") || vin.contains("Q")) {
            throw new IllegalArgumentException("VIN码不能包含I、O、Q");
        }
        if (!vin.startsWith("L")) {
            throw new IllegalArgumentException("VIN码必须以L开头（中国制造）");
        }
    }

    private void validateNanjingCoordinates(Double latitude, Double longitude) {
        if (latitude < 31.2340 || latitude > 32.6200) {
            throw new IllegalArgumentException("纬度范围必须在31.2340-32.6200之间");
        }
        if (longitude < 118.3000 || longitude > 119.2000) {
            throw new IllegalArgumentException("经度范围必须在118.3000-119.2000之间");
        }
    }

    private String calculateGisGrid(Double latitude, Double longitude) {
        int gridNum = (int) ((latitude - 31.2340) / (32.6200 - 31.2340) * 10) * 10 +
                      (int) ((longitude - 118.3000) / (119.2000 - 118.3000) * 10);
        gridNum = Math.min(Math.max(gridNum + 1, 1), 100);
        return String.format("GIS-GRID-%03d", gridNum);
    }

    public String generateVin() {
        String chars = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
        StringBuilder vin = new StringBuilder("LHG"); // WMI for China
        for (int i = 0; i < 14; i++) {
            vin.append(chars.charAt(random.nextInt(chars.length())));
        }
        return vin.toString();
    }
}
