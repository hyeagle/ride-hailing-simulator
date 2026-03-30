package org.example.controller;

import org.example.entity.Station;
import org.example.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<Station> getAll() {
        return stationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getById(@PathVariable String id) {
        return stationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public List<Station> getByType(@PathVariable Station.StationType type) {
        return stationService.findByType(type);
    }

    @GetMapping("/search")
    public List<Station> searchByName(@RequestParam String name) {
        return stationService.findByNameContaining(name);
    }

    @GetMapping("/coordinates")
    public List<Station> getByCoordinates(
            @RequestParam Double latMin, @RequestParam Double latMax,
            @RequestParam Double lngMin, @RequestParam Double lngMax) {
        return stationService.findByCoordinates(latMin, latMax, lngMin, lngMax);
    }

    @GetMapping("/count")
    public long getCount() {
        return stationService.count();
    }

    @PostMapping
    public ResponseEntity<Station> create(@RequestBody Station station) {
        try {
            return ResponseEntity.ok(stationService.create(station));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> update(@PathVariable String id, @RequestBody Station station) {
        try {
            return ResponseEntity.ok(stationService.update(id, station));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            stationService.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
