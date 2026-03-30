package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * 车辆状态枚举
     */
    public enum VehicleStatus {
        履约中,
        巡游中,
        维护中
    }
}
