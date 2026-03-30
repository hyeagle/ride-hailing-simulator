package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.impl.OrderDispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单派单控制器
 */
@RestController
@RequestMapping("/api/order-dispatch")
@RequiredArgsConstructor
public class OrderDispatchController {

    private final OrderDispatchService orderDispatchService;

    /**
     * 获取派单中的车辆信息
     */
    @GetMapping("/info")
    public List<Map<String, Object>> getDispatchInfo() {
        return orderDispatchService.getDispatchingVehicles();
    }

    /**
     * 手动触发派单
     */
    @PostMapping("/trigger")
    public String triggerDispatch() {
        return orderDispatchService.triggerDispatch();
    }

    /**
     * 模拟创建订单并派单
     */
    @PostMapping("/simulate")
    public ResponseEntity<Integer> simulateAndDispatch(@RequestParam int count) {
        try {
            int created = orderDispatchService.simulateAndDispatch(count);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 车辆到达接乘点
     */
    @PostMapping("/arrived-pickup/{vehicleId}")
    public ResponseEntity<Void> arrivedPickup(@PathVariable String vehicleId) {
        try {
            orderDispatchService.vehicleArrivedPickup(vehicleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 车辆到达目的地
     */
    @PostMapping("/arrived-destination/{vehicleId}")
    public ResponseEntity<Void> arrivedDestination(@PathVariable String vehicleId) {
        try {
            orderDispatchService.vehicleArrivedDestination(vehicleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
