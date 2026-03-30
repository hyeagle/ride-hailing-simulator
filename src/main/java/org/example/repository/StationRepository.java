package org.example.repository;

import org.example.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, String> {

    List<Station> findByType(Station.StationType type);

    List<Station> findByLatitudeBetweenAndLongitudeBetween(
            Double latMin, Double latMax, Double lngMin, Double lngMax);

    List<Station> findByNameContaining(String name);
}
