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
     * 起始地点（地址或坐标）
     */
    private String origin;
    
    /**
     * 目的地点（地址或坐标）
     */
    private String destination;
    
    /**
     * 高德地图 API Key
     */
    private String apiKey;

    /**
     * 是否使用坐标模式（默认false，使用地址）
     */
    private boolean coordinateMode = false;
}
