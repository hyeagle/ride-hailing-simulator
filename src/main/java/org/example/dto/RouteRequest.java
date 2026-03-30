package org.example.dto;

import lombok.Data;

/**
 * 路线规划请求参数
 * 
 * @author example
 */
@Data
public class RouteRequest {
    
    /**
     * 起始地点
     */
    private String origin;
    
    /**
     * 目的地点
     */
    private String destination;
    
    /**
     * 高德地图 API Key
     */
    private String apiKey;
}
