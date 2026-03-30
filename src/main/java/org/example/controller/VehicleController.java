package org.example.controller;

import org.example.entity.Vehicle;
import org.example.service.impl.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<Vehicle> getAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable String id) {
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vin/{vin}")
    public ResponseEntity<Vehicle> getByVin(@PathVariable String vin) {
        return vehicleService.findByVin(vin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Vehicle> getByStatus(@PathVariable Vehicle.VehicleStatus status) {
        return vehicleService.findByStatus(status);
    }

    @GetMapping("/battery")
    public List<Vehicle> getByBattery(@RequestParam Integer min, @RequestParam Integer max) {
        return vehicleService.findByBatteryRange(min, max);
    }

    @GetMapping("/gis-grid/{gisGrid}")
    public List<Vehicle> getByGisGrid(@PathVariable String gisGrid) {
        return vehicleService.findByGisGrid(gisGrid);
    }

    @GetMapping("/search")
    public List<Vehicle> search(
            @RequestParam(required = false) String vin,
            @RequestParam(required = false) String gisGrid,
            @RequestParam(required = false) Integer minBattery,
            @RequestParam(required = false) Integer maxBattery) {
        return vehicleService.search(vin, gisGrid, minBattery, maxBattery);
    }

    @PostMapping("/power-on")
    public ResponseEntity<Integer> powerOnVehicles(@RequestParam(defaultValue = "1") int count) {
        try {
            int actualCount = vehicleService.powerOnVehicles(count);
            return ResponseEntity.ok(actualCount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle) {
        try {
            return ResponseEntity.ok(vehicleService.create(vehicle));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate-vin")
    public String generateVin() {
        return vehicleService.generateVin();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable String id, @RequestBody Vehicle vehicle) {
        try {
            return ResponseEntity.ok(vehicleService.update(id, vehicle));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            vehicleService.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
