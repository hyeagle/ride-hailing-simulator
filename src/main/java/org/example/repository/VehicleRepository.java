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

    List<Vehicle> findByBatteryPercentBetween(Integer min, Integer max);

    List<Vehicle> findByGisGrid(String gisGrid);

    List<Vehicle> findByLatitudeBetweenAndLongitudeBetween(
            Double latMin, Double latMax, Double lngMin, Double lngMax);

    boolean existsByOrderId(String orderId);
}
