package com.governance.iotcollection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 采集任务响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT collection task response")
public class IotCollectionTaskResponse {

    @Schema(description = "任务 ID", example = "1")
    private Long id;

    @Schema(description = "任务名称", example = "温度采集任务")
    private String taskName;

    @Schema(description = "设备 ID", example = "1")
    private Long deviceId;

    @Schema(description = "设备编码", example = "DEVICE_001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "温度传感器-01")
    private String deviceName;

    @Schema(description = "产品编码", example = "TEMP_SENSOR_V1")
    private String productKey;

    @Schema(description = "产品名称", example = "温度传感器产品")
    private String productName;

    @Schema(description = "采集类型", example = "POLLING")
    private String collectionType;

    @Schema(description = "调度类型", example = "CRON")
    private String scheduleType;

    @Schema(description = "Cron 表达式")
    private String cronExpression;

    @Schema(description = "轮询间隔秒数")
    private Integer pollIntervalSeconds;

    @Schema(description = "数据来源类型")
    private String sourceType;

    @Schema(description = "数据格式")
    private String dataFormat;

    @Schema(description = "采集配置 JSON")
    private String configJson;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
