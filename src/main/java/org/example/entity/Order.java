package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`order`")
public class Order {

    @Id
    @Column(length = 30)
    private String id; // nj-202603271630-0001

    @Column(nullable = false, length = 15)
    private String originStationId; // 起始站点ID

    @Column(nullable = false, length = 15)
    private String destinationStationId; // 终止站点ID

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime estimatedArrivalTime;

    @Column(nullable = false)
    private Double estimatedRevenue; // 预计完单收益（元）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 10)
    private String vehicleId; // 履约车辆ID

    private LocalDateTime actualArrivalTime;
    
    /**
     * 接乘开始时间
     */
    private LocalDateTime pickupStartTime;
    
    /**
     * 接乘到达时间
     */
    private LocalDateTime pickupArrivalTime;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        待派单,
        待接乘,
        进行中,
        已完成,
        已取消
    }
}
