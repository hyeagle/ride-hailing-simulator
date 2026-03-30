package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 订单派单配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "order-dispatch")
public class OrderDispatchConfig {
    
    /**
     * 是否启用自动派单
     */
    private boolean enabled = true;
    
    /**
     * 派单间隔时间（毫秒），默认2分钟
     */
    private Long intervalMs = 120000L;
    
    /**
     * 车辆接乘速度（km/h），默认30
     */
    private Double pickupSpeedKmh = 30.0;
    
    /**
     * 车辆履约速度（km/h），默认40
     */
    private Double deliverySpeedKmh = 40.0;
}
