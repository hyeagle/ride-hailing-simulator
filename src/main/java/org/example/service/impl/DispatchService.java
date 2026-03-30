package org.example.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DispatchConfig;
import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;
import org.example.entity.Station;
import org.example.entity.Vehicle;
import org.example.repository.StationRepository;
import org.example.repository.VehicleRepository;
import org.example.service.RouteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 调度服务
 * 负责将"巡游中"的车辆调度到随机站点
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final RouteService routeService;
    private final DispatchConfig dispatchConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定时调度任务
     * 默认每2分钟执行一次，可通过配置修改
     */
    @Scheduled(fixedDelayString = "${dispatch.interval-ms:120000}")
    @Transactional
    public void dispatchVehicles() {
        if (!dispatchConfig.isEnabled()) {
            return;
        }

        log.info("开始执行车辆调度任务...");

        // 1. 查询所有"巡游中"状态的车辆
        List<Vehicle> cruisingVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.巡游中);
        
        if (cruisingVehicles.isEmpty()) {
            log.info("没有巡游中的车辆，跳过调度");
            return;
        }

        log.info("发现 {} 辆巡游中的车辆", cruisingVehicles.size());

        // 2. 获取所有站点
        List<Station> stations = stationRepository.findAll();
        if (stations.isEmpty()) {
            log.warn("没有可用的站点，跳过调度");
            return;
        }

        Random random = new Random();

        // 3. 为每辆车分配随机站点
        for (Vehicle vehicle : cruisingVehicles) {
            try {
                // 随机选择一个站点
                Station targetStation = stations.get(random.nextInt(stations.size()));
                
                // 规划路线
                RouteRequest routeRequest = new RouteRequest();
                routeRequest.setOrigin(vehicle.getLongitude() + "," + vehicle.getLatitude());
                routeRequest.setDestination(targetStation.getLongitude() + "," + targetStation.getLatitude());
                
                RouteResponse routeResponse = routeService.planRoute(routeRequest);
                
                if (routeResponse.getStatus() == 200 && routeResponse.getPath() != null) {
                    // 将路径转换为JSON存储
                    String pathJson = convertPathToJson(routeResponse.getPath());
                    
                    // 更新车辆调度信息，状态改为"调度中"
                    vehicle.setStatus(Vehicle.VehicleStatus.调度中);
                    vehicle.setTargetStationId(targetStation.getId());
                    vehicle.setDispatchPath(pathJson);
                    vehicle.setDispatchProgress(0);
                    vehicle.setDispatchStartTime(LocalDateTime.now());
                    
                    vehicleRepository.save(vehicle);
                    
                    log.info("车辆 {} 已调度到站点 {}，状态改为调度中", vehicle.getId(), targetStation.getName());
                } else {
                    log.warn("车辆 {} 路线规划失败: {}", vehicle.getId(), routeResponse.getMessage());
                }
            } catch (Exception e) {
                log.error("调度车辆 {} 时发生错误", vehicle.getId(), e);
            }
        }
        
        log.info("车辆调度任务完成");
    }

    /**
     * 车辆到达目标站点，更新数据库位置
     * @param vehicleId 车辆ID
     */
    @Transactional
    public void vehicleArrived(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在: " + vehicleId));
        
        if (vehicle.getTargetStationId() == null) {
            log.warn("车辆 {} 没有调度任务", vehicleId);
            return;
        }
        
        Station targetStation = stationRepository.findById(vehicle.getTargetStationId()).orElse(null);
        if (targetStation != null) {
            // 更新车辆位置到目标站点
            vehicle.setLongitude(targetStation.getLongitude());
            vehicle.setLatitude(targetStation.getLatitude());
            vehicle.setGisGrid(calculateGisGrid(targetStation.getLatitude(), targetStation.getLongitude()));
            log.info("车辆 {} 已到达目标站点 {}", vehicleId, targetStation.getName());
        }
        
        // 清除调度信息，状态恢复为"巡游中"
        vehicle.setStatus(Vehicle.VehicleStatus.巡游中);
        vehicle.setTargetStationId(null);
        vehicle.setDispatchPath(null);
        vehicle.setDispatchProgress(null);
        vehicle.setDispatchStartTime(null);
        
        vehicleRepository.save(vehicle);
    }

    /**
     * 获取所有正在调度中的车辆信息
     */
    public List<org.example.dto.DispatchInfo> getDispatchInfo() {
        // 查询"调度中"状态的车辆
        List<Vehicle> dispatchingVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.调度中);

        List<org.example.dto.DispatchInfo> result = new ArrayList<>();
        
        for (Vehicle vehicle : dispatchingVehicles) {
            try {
                Station targetStation = stationRepository.findById(vehicle.getTargetStationId()).orElse(null);
                List<double[]> path = parsePathFromJson(vehicle.getDispatchPath());
                
                result.add(org.example.dto.DispatchInfo.builder()
                        .vehicleId(vehicle.getId())
                        .vin(vehicle.getVin())
                        .longitude(vehicle.getLongitude())
                        .latitude(vehicle.getLatitude())
                        .targetStationId(vehicle.getTargetStationId())
                        .targetStationName(targetStation != null ? targetStation.getName() : null)
                        .targetLongitude(targetStation != null ? targetStation.getLongitude() : null)
                        .targetLatitude(targetStation != null ? targetStation.getLatitude() : null)
                        .path(path)
                        .progress(vehicle.getDispatchProgress())
                        .startTime(vehicle.getDispatchStartTime())
                        .build());
            } catch (Exception e) {
                log.error("获取车辆 {} 调度信息时发生错误", vehicle.getId(), e);
            }
        }
        
        return result;
    }

    private String convertPathToJson(Object path) {
        try {
            return objectMapper.writeValueAsString(path);
        } catch (JsonProcessingException e) {
            log.error("转换路径到JSON失败", e);
            return null;
        }
    }

    private List<double[]> parsePathFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<double[]>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析路径JSON失败", e);
            return null;
        }
    }
    
    private String calculateGisGrid(Double latitude, Double longitude) {
        int gridNum = (int) ((latitude - 31.2340) / (32.6200 - 31.2340) * 10) * 10 +
                      (int) ((longitude - 118.3000) / (119.2000 - 118.3000) * 10);
        gridNum = Math.min(Math.max(gridNum + 1, 1), 100);
        return String.format("GIS-GRID-%03d", gridNum);
    }
}
