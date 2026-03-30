package org.example.controller;

import org.example.entity.ParkingLot;
import org.example.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping
    public List<ParkingLot> getAll() {
        return parkingLotService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getById(@PathVariable String id) {
        return parkingLotService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/gis-grid/{gisGrid}")
    public List<ParkingLot> getByGisGrid(@PathVariable String gisGrid) {
        return parkingLotService.findByGisGrid(gisGrid);
    }

    @GetMapping("/capacity")
    public List<ParkingLot> getByCapacity(@RequestParam Integer min, @RequestParam Integer max) {
        return parkingLotService.findByCapacityRange(min, max);
    }

    @GetMapping("/coordinates")
    public List<ParkingLot> getByCoordinates(
            @RequestParam Double latMin, @RequestParam Double latMax,
            @RequestParam Double lngMin, @RequestParam Double lngMax) {
        return parkingLotService.findByCoordinates(latMin, latMax, lngMin, lngMax);
    }

    @PostMapping
    public ResponseEntity<ParkingLot> create(@RequestBody ParkingLot parkingLot) {
        try {
            return ResponseEntity.ok(parkingLotService.create(parkingLot));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingLot> update(@PathVariable String id, @RequestBody ParkingLot parkingLot) {
        try {
            return ResponseEntity.ok(parkingLotService.update(id, parkingLot));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            parkingLotService.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
