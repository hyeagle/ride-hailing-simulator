package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchInfo {

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 车辆VIN
     */
    private String vin;

    /**
     * 当前经度
     */
    private Double longitude;

    /**
     * 当前纬度
     */
    private Double latitude;

    /**
     * 目标站点ID
     */
    private String targetStationId;

    /**
     * 目标站点名称
     */
    private String targetStationName;

    /**
     * 目标站点经度
     */
    private Double targetLongitude;

    /**
     * 目标站点纬度
     */
    private Double targetLatitude;

    /**
     * 路径点列表 [[lng, lat], ...]
     */
    private List<double[]> path;

    /**
     * 调度进度 (0-100)
     */
    private Integer progress;

    /**
     * 调度开始时间
     */
    private LocalDateTime startTime;
}
