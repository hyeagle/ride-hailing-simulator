package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @Column(length = 10)
    private String id; // C0001, C0002, ...

    @Column(nullable = false, unique = true, length = 17)
    private String vin; // 17位VIN码

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(length = 30)
    private String orderId; // 履约中时关联订单ID

    @Column(nullable = false)
    private Integer batteryPercent; // 0-100

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 20)
    private String gisGrid; // GIS-GRID-XXX

    // ========== 调度相关字段 ==========
    
    /**
     * 目标站点ID
     */
    @Column(length = 15)
    private String targetStationId;

    /**
     * 调度路线（JSON格式存储路径点）
     */
    @Column(columnDefinition = "TEXT")
    private String dispatchPath;

    /**
     * 调度进度 (0-100)
     */
    private Integer dispatchProgress;

    /**
     * 调度开始时间
     */
    private LocalDateTime dispatchStartTime;

    // ========== 派单相关字段 ==========
    
    /**
     * 接乘路线（JSON格式存储路径点）
     */
    @Column(columnDefinition = "TEXT")
    private String pickupPath;
    
    /**
     * 接乘进度 (0-100)
     */
    private Integer pickupProgress;
    
    /**
     * 履约路线（JSON格式存储路径点）
     */
    @Column(columnDefinition = "TEXT")
    private String deliveryPath;
    
    /**
     * 履约进度 (0-100)
     */
    private Integer deliveryProgress;
    
    /**
     * 派单开始时间
     */
    private LocalDateTime dispatchOrderStartTime;
    
    /**
     * 车辆状态枚举
     */
    public enum VehicleStatus {
        休息中,
        履约中,
        巡游中,
        调度中,
        接乘中,
        维护中
    }
}
