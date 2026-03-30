package org.example.service.impl;

import org.example.entity.Station;
import org.example.repository.OrderRepository;
import org.example.repository.StationRepository;
import org.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    @Transactional
    public Station create(Station station) {
        // 验证经纬度
        validateNanjingCoordinates(station.getLatitude(), station.getLongitude());

        // 生成ID
        if (station.getId() == null || station.getId().isEmpty()) {
            station.setId(generateStationId());
        }
        return stationRepository.save(station);
    }

    public Optional<Station> findById(String id) {
        return stationRepository.findById(id);
    }

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public List<Station> findByType(Station.StationType type) {
        return stationRepository.findByType(type);
    }

    public List<Station> findByNameContaining(String name) {
        return stationRepository.findByNameContaining(name);
    }

    public List<Station> findByCoordinates(Double latMin, Double latMax, Double lngMin, Double lngMax) {
        return stationRepository.findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, lngMin, lngMax);
    }

    @Transactional
    public Station update(String id, Station updated) {
        return stationRepository.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getLatitude() != null && updated.getLongitude() != null) {
                validateNanjingCoordinates(updated.getLatitude(), updated.getLongitude());
                existing.setLatitude(updated.getLatitude());
                existing.setLongitude(updated.getLongitude());
            }
            if (updated.getType() != null) existing.setType(updated.getType());
            if (updated.getRemark() != null) existing.setRemark(updated.getRemark());
            return stationRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("站点不存在: " + id));
    }

    @Transactional
    public void delete(String id) {
        // 检查是否有订单关联
        if (orderRepository.existsByOriginStationIdOrDestinationStationId(id, id)) {
            throw new IllegalArgumentException("站点有关联订单，无法删除");
        }
        stationRepository.deleteById(id);
    }

    private void validateNanjingCoordinates(Double latitude, Double longitude) {
        if (latitude < 31.2340 || latitude > 32.6200) {
            throw new IllegalArgumentException("纬度范围必须在31.2340-32.6200之间");
        }
        if (longitude < 118.3000 || longitude > 119.2000) {
            throw new IllegalArgumentException("经度范围必须在118.3000-119.2000之间");
        }
    }

    private String generateStationId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder id = new StringBuilder("NJ");
        for (int i = 0; i < 8; i++) {
            id.append(chars.charAt(random.nextInt(chars.length())));
        }
        return id.toString();
    }

    public long count() {
        return stationRepository.count();
    }
}
