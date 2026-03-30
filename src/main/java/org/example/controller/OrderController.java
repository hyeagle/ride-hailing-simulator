package org.example.controller;

import org.example.entity.Order;
import org.example.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<Order> getAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Order> getByStatus(@PathVariable Order.OrderStatus status) {
        return orderService.findByStatus(status);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<Order> getByVehicle(@PathVariable String vehicleId) {
        return orderService.findByVehicle(vehicleId);
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        try {
            return ResponseEntity.ok(orderService.create(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<Order> assignVehicle(@PathVariable String id, @RequestParam String vehicleId) {
        try {
            return ResponseEntity.ok(orderService.assignVehicle(id, vehicleId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Order> complete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(orderService.complete(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable String id) {
        try {
            return ResponseEntity.ok(orderService.cancel(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
