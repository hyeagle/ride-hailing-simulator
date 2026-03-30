package org.example.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;
import org.example.service.RouteService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class T3RouteServiceImpl implements RouteService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GEOCODE_URL = "http://gateway.t3go.com.cn/gis-map-api/lbs/v1/geocode/geo";
    private static final String ROUTE_URL = "http://gateway.t3go.com.cn/gis-map-api/lbs/v2/direction/driving";

    @Override
    public String[] geocode(String address, String apiKey) throws Exception {
        // 构建请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("address", address);
        requestBody.put("supplier", "高德");

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(GEOCODE_URL, requestEntity, String.class);

        // 解析响应
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode data = root.path("data");

        if (data.isArray() && !data.isEmpty()) {
            JsonNode first = data.get(0);
            double longitude = first.path("longitude").asDouble();
            double latitude = first.path("latitude").asDouble();
            log.info("地理编码结果, 地址: {}, 经度: {}, 纬度: {}", address, longitude, latitude);
            return new String[]{String.valueOf(longitude), String.valueOf(latitude)};
        }
        throw new RuntimeException("地理编码失败");
    }

    @Override
    public RouteResponse planRoute(RouteRequest request) {
        try {
            // 1. 将地址或坐标转换为坐标
            String[] originCoords;
            String[] destCoords;
            
            // 检查是否是坐标格式（包含逗号且为数字）
            if (isCoordinateFormat(request.getOrigin())) {
                originCoords = request.getOrigin().split(",");
            } else {
                originCoords = geocode(request.getOrigin(), request.getApiKey());
            }
            
            if (isCoordinateFormat(request.getDestination())) {
                destCoords = request.getDestination().split(",");
            } else {
                destCoords = geocode(request.getDestination(), request.getApiKey());
            }

            // 2. 构建请求体
            Map<String, Object> origin = new HashMap<>();
            origin.put("lng", Double.parseDouble(originCoords[0].trim()));
            origin.put("lat", Double.parseDouble(originCoords[1].trim()));

            Map<String, Object> dest = new HashMap<>();
            dest.put("lng", Double.parseDouble(destCoords[0].trim()));
            dest.put("lat", Double.parseDouble(destCoords[1].trim()));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("origin", origin);
            requestBody.put("dest", dest);
            requestBody.put("waypoints", new ArrayList<>());

            // 3. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 4. 发送 POST 请求
            ResponseEntity<String> response = restTemplate.postForEntity(ROUTE_URL, requestEntity, String.class);
            log.info("路线规划响应: {}", response.getBody());

            // 5. 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            JsonNode routes = data.path("routes");

            if (routes.isArray() && !routes.isEmpty()) {
                JsonNode firstRoute = routes.get(0);
                int distance = firstRoute.path("distance").asInt();
                int duration = firstRoute.path("duration").asInt();

                // 解析 polyline 为坐标数组
                List<double[]> pathCoordinates = new ArrayList<>();
                JsonNode steps = firstRoute.path("steps");
                if (steps.isArray()) {
                    for (JsonNode step : steps) {
                        String polylineStr = step.path("polyline").asText();
                        if (polylineStr != null && !polylineStr.isEmpty()) {
                            String[] points = polylineStr.split(";");
                            for (String point : points) {
                                String[] coords = point.split(",");
                                if (coords.length == 2) {
                                    double lng = Double.parseDouble(coords[0]);
                                    double lat = Double.parseDouble(coords[1]);
                                    pathCoordinates.add(new double[]{lng, lat});
                                }
                            }
                        }
                    }
                }

                return RouteResponse.builder()
                        .status(200)
                        .message("成功")
                        .distance(distance)
                        .duration(duration)
                        .path(pathCoordinates)
                        .build();
            }

            return RouteResponse.builder()
                    .status(500)
                    .message("路线规划失败")
                    .build();
        } catch (Exception e) {
            return RouteResponse.builder()
                    .status(500)
                    .message("路线规划异常: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 检查字符串是否为坐标格式（如 "118.795,32.05"）
     */
    private boolean isCoordinateFormat(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        String[] parts = str.split(",");
        if (parts.length != 2) {
            return false;
        }
        try {
            Double.parseDouble(parts[0].trim());
            Double.parseDouble(parts[1].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
