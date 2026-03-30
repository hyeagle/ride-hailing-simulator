package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RouteRequest;
import org.example.dto.RouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线规划服务
 * 
 * @author example
 */
@Slf4j
//@Service
public class GDRouteServiceImpl implements RouteService {

    private static final String GEO_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String DIRECTION_URL = "https://restapi.amap.com/v3/direction/driving";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GDRouteServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 地理编码：地址转坐标
     */
    public String[] geocode(String address, String apiKey) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl(GEO_URL)
                .queryParam("key", apiKey)
                .queryParam("address", address)
                .queryParam("city", "北京")  // 添加城市参数提高准确性
                .toUriString();
        
        log.info("地理编码请求: {}", url);
        
        String response = restTemplate.getForObject(url, String.class);
        log.info("地理编码响应: {}", response);
        
        JsonNode root = objectMapper.readTree(response);
        
        // 检查API状态
        String status = root.path("status").asText();
        String info = root.path("info").asText();
        String infocode = root.path("infocode").asText();
        
        if (!"1".equals(status)) {
            String errorMsg = String.format("高德API错误 - status: %s, info: %s, infocode: %s", 
                    status, info, infocode);
            log.error(errorMsg);
            
            // 根据错误码给出具体提示
            if ("10001".equals(infocode)) {
                throw new RuntimeException("API Key 不正确或未激活，请检查您的 Web服务 Key");
            } else if ("10002".equals(infocode)) {
                throw new RuntimeException("API Key 没有权限，请确认已开通 Web服务 权限");
            } else if ("10003".equals(infocode)) {
                throw new RuntimeException("API Key 每日调用次数超限");
            } else if ("10004".equals(infocode)) {
                throw new RuntimeException("API Key 访问服务被拒绝");
            } else {
                throw new RuntimeException(errorMsg);
            }
        }
        
        if (root.path("geocodes").isArray() && root.path("geocodes").size() > 0) {
            String location = root.path("geocodes").get(0).path("location").asText();
            if (location != null && !location.isEmpty()) {
                log.info("地址 '{}' 坐标: {}", address, location);
                return location.split(",");
            }
        }
        
        throw new RuntimeException("无法找到地址: " + address + "，请检查地址名称是否正确");
    }

    /**
     * 规划驾车路线
     */
    public RouteResponse planRoute(RouteRequest request) {
        try {
            log.info("开始路线规划 - 起点: {}, 终点: {}", request.getOrigin(), request.getDestination());
            
            // 1. 地理编码 - 获取起点坐标
            String[] origin = geocode(request.getOrigin(), request.getApiKey());
            log.info("起点坐标: {},{}", origin[0], origin[1]);
            
            // 2. 地理编码 - 获取终点坐标
            String[] destination = geocode(request.getDestination(), request.getApiKey());
            log.info("终点坐标: {},{}", destination[0], destination[1]);
            
            // 3. 路线规划
            String url = UriComponentsBuilder.fromHttpUrl(DIRECTION_URL)
                    .queryParam("key", request.getApiKey())
                    .queryParam("origin", origin[0] + "," + origin[1])
                    .queryParam("destination", destination[0] + "," + destination[1])
                    .queryParam("extensions", "all")
                    .toUriString();
            
            log.info("路线规划请求: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.info("路线规划响应: {}", response);
            
            JsonNode root = objectMapper.readTree(response);
            
            // 检查API状态
            String status = root.path("status").asText();
            String info = root.path("info").asText();
            String infocode = root.path("infocode").asText();
            
            if (!"1".equals(status)) {
                String errorMsg = String.format("路线规划API错误 - status: %s, info: %s, infocode: %s", 
                        status, info, infocode);
                log.error(errorMsg);
                return RouteResponse.builder()
                        .status(0)
                        .message(errorMsg)
                        .build();
            }
            
            JsonNode route = root.path("route");
            JsonNode paths = route.path("paths");
            
            if (paths.isArray() && paths.size() > 0) {
                JsonNode firstPath = paths.get(0);
                
                // 提取距离和时间
                Integer distance = firstPath.path("distance").asInt();
                Integer duration = firstPath.path("duration").asInt();
                
                log.info("路线距离: {}米, 时间: {}秒", distance, duration);
                
                // 提取路径点
                List<double[]> pathPoints = new ArrayList<>();
                JsonNode steps = firstPath.path("steps");
                
                if (steps.isArray()) {
                    for (JsonNode step : steps) {
                        String polyline = step.path("polyline").asText();
                        String[] points = polyline.split(";");
                        for (String point : points) {
                            String[] coords = point.split(",");
                            if (coords.length == 2) {
                                pathPoints.add(new double[]{
                                    Double.parseDouble(coords[0]),
                                    Double.parseDouble(coords[1])
                                });
                            }
                        }
                    }
                }
                
                log.info("路线规划成功，共 {} 个路径点", pathPoints.size());
                
                return RouteResponse.builder()
                        .status(1)
                        .message("成功")
                        .distance(distance)
                        .duration(duration)
                        .path(pathPoints)
                        .build();
            }
            
            return RouteResponse.builder()
                    .status(0)
                    .message("未找到可用路线")
                    .build();
                    
        } catch (Exception e) {
            log.error("路线规划失败", e);
            return RouteResponse.builder()
                    .status(0)
                    .message("路线规划失败: " + e.getMessage())
                    .build();
        }
    }
}
