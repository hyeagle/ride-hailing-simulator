package org.example.controller;

import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;
import org.example.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路线规划控制器
 * 
 * @author example
 */
@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    /**
     * 规划路线
     * 
     * @param request 路线请求参数
     * @return 路线规划结果
     */
    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planRoute(@RequestBody RouteRequest request) {
        RouteResponse response = routeService.planRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
