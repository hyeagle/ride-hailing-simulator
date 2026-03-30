package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站点实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "station")
public class Station {

    @Id
    @Column(length = 15)
    private String id; // 8-12位随机字符串

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StationType type;

    private String remark;

    /**
     * 站点类型枚举
     */
    public enum StationType {
        商圈,
        景区,
        医院,
        社区,
        交通枢纽
    }
}
