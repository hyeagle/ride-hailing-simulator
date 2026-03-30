package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 停车场实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parking_lot")
public class ParkingLot {

    @Id
    @Column(length = 10)
    private String id; // P001, P002, ...

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String gisGrid; // GIS-GRID-XXX

    @Column(nullable = false)
    private Integer capacity; // 20-50 整十数值

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String remark;
}
