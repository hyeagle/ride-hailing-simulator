package org.example.service;

import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;

public interface RouteService {

    /**
     * 地理编码：地址转坐标
     */
    String[] geocode(String address, String apiKey) throws Exception;

    /**
     * 规划驾车路线
     */
    RouteResponse planRoute(RouteRequest request);
}
