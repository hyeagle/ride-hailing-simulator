package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.DispatchInfo;
import org.example.service.impl.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度控制器
 */
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    /**
     * 获取所有正在调度中的车辆信息
     */
    @GetMapping("/info")
    public ResponseEntity<List<DispatchInfo>> getDispatchInfo() {
        return ResponseEntity.ok(dispatchService.getDispatchInfo());
    }

    /**
     * 手动触发调度（用于测试）
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerDispatch() {
        dispatchService.dispatchVehicles();
        return ResponseEntity.ok("调度任务已触发");
    }

    /**
     * 车辆到达目标站点
     * @param vehicleId 车辆ID
     */
    @PostMapping("/arrived/{vehicleId}")
    public ResponseEntity<String> vehicleArrived(@PathVariable String vehicleId) {
        try {
            dispatchService.vehicleArrived(vehicleId);
            return ResponseEntity.ok("车辆已到达");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
