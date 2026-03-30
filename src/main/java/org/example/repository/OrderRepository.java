package org.example.repository;

import org.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByOriginStationId(String stationId);

    List<Order> findByDestinationStationId(String stationId);

    List<Order> findByVehicleId(String vehicleId);

    List<Order> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByOriginStationIdOrDestinationStationId(String originId, String destId);
}
