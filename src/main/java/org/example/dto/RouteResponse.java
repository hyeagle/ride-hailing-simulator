package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路线规划响应结果
 * 
 * @author example
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    
    /**
     * 状态码
     */
    private Integer status;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 距离（米）
     */
    private Integer distance;
    
    /**
     * 时间（秒）
     */
    private Integer duration;
    
    /**
     * 路径点列表
     */
    private Object path;
}
