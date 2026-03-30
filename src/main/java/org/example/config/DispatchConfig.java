package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 调度配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dispatch")
public class DispatchConfig {

    /**
     * 调度间隔（分钟），默认2分钟
     */
    private int intervalMinutes = 2;

    /**
     * 是否启用调度，默认启用
     */
    private boolean enabled = true;

    /**
     * 车辆移动速度（公里/小时），默认30km/h
     */
    private double speedKmh = 30;
}
