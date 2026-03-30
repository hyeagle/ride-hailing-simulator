package org.example.repository;

import org.example.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    List<Vehicle> findByStatusIn(List<Vehicle.VehicleStatus> statuses);

    List<Vehicle> findByBatteryPercentBetween(Integer min, Integer max);

    List<Vehicle> findByGisGrid(String gisGrid);

    List<Vehicle> findByLatitudeBetweenAndLongitudeBetween(
            Double latMin, Double latMax, Double lngMin, Double lngMax);

    boolean existsByOrderId(String orderId);

    // 综合查询：VIN模糊匹配 + 网格 + 电量范围
    List<Vehicle> findByVinContainingAndGisGridAndBatteryPercentBetween(
            String vin, String gisGrid, Integer minBattery, Integer maxBattery);

    // VIN模糊匹配 + 网格
    List<Vehicle> findByVinContainingAndGisGrid(String vin, String gisGrid);

    // VIN模糊匹配 + 电量范围
    List<Vehicle> findByVinContainingAndBatteryPercentBetween(String vin, Integer minBattery, Integer maxBattery);

    // 网格 + 电量范围
    List<Vehicle> findByGisGridAndBatteryPercentBetween(String gisGrid, Integer minBattery, Integer maxBattery);

    // VIN模糊匹配
    List<Vehicle> findByVinContaining(String vin);

}
