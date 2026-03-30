package org.example.service;

import org.example.entity.ParkingLot;
import org.example.repository.ParkingLotRepository;
import org.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final VehicleRepository vehicleRepository;
    private int idCounter = 1;

    @Transactional
    public ParkingLot create(ParkingLot parkingLot) {
        // 验证容量范围 (20-50 整十)
        if (parkingLot.getCapacity() < 20 || parkingLot.getCapacity() > 50 || parkingLot.getCapacity() % 10 != 0) {
            throw new IllegalArgumentException("容纳车辆数必须为20-50之间的整十数值");
        }
        // 验证经纬度范围 (南京)
        validateNanjingCoordinates(parkingLot.getLatitude(), parkingLot.getLongitude());

        // 生成ID
        if (parkingLot.getId() == null || parkingLot.getId().isEmpty()) {
            parkingLot.setId(String.format("P%03d", idCounter++));
        }
        return parkingLotRepository.save(parkingLot);
    }

    public Optional<ParkingLot> findById(String id) {
        return parkingLotRepository.findById(id);
    }

    public List<ParkingLot> findAll() {
        return parkingLotRepository.findAll();
    }

    public List<ParkingLot> findByGisGrid(String gisGrid) {
        return parkingLotRepository.findByGisGrid(gisGrid);
    }

    public List<ParkingLot> findByCapacityRange(Integer min, Integer max) {
        return parkingLotRepository.findByCapacityBetween(min, max);
    }

    public List<ParkingLot> findByCoordinates(Double latMin, Double latMax, Double lngMin, Double lngMax) {
        return parkingLotRepository.findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, lngMin, lngMax);
    }

    @Transactional
    public ParkingLot update(String id, ParkingLot updated) {
        return parkingLotRepository.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getGisGrid() != null) existing.setGisGrid(updated.getGisGrid());
            if (updated.getCapacity() != null) {
                if (updated.getCapacity() < 20 || updated.getCapacity() > 50 || updated.getCapacity() % 10 != 0) {
                    throw new IllegalArgumentException("容纳车辆数必须为20-50之间的整十数值");
                }
                existing.setCapacity(updated.getCapacity());
            }
            if (updated.getLatitude() != null && updated.getLongitude() != null) {
                validateNanjingCoordinates(updated.getLatitude(), updated.getLongitude());
                existing.setLatitude(updated.getLatitude());
                existing.setLongitude(updated.getLongitude());
            }
            if (updated.getRemark() != null) existing.setRemark(updated.getRemark());
            return parkingLotRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("停车场不存在: " + id));
    }

    @Transactional
    public void delete(String id) {
        // 检查是否有车辆停放在此停车场
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("停车场不存在: " + id));
        // 注意：实际应该检查车辆是否在停车场范围内
        parkingLotRepository.deleteById(id);
    }

    private void validateNanjingCoordinates(Double latitude, Double longitude) {
        if (latitude < 31.2340 || latitude > 32.6200) {
            throw new IllegalArgumentException("纬度范围必须在31.2340-32.6200之间");
        }
        if (longitude < 118.3000 || longitude > 119.2000) {
            throw new IllegalArgumentException("经度范围必须在118.3000-119.2000之间");
        }
    }
}
