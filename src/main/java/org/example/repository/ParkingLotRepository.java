package org.example.repository;

import org.example.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, String> {

    List<ParkingLot> findByGisGrid(String gisGrid);

    List<ParkingLot> findByCapacityBetween(Integer min, Integer max);

    List<ParkingLot> findByLatitudeBetweenAndLongitudeBetween(
            Double latMin, Double latMax, Double lngMin, Double lngMax);
}
