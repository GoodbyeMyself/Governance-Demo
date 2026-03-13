package com.governance.iotcollection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 采集任务新增或更新请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT collection task create/update request")
public class IotCollectionTaskRequest {

    @Schema(description = "任务名称", example = "温度采集任务")
    @NotBlank(message = "taskName is required")
    @Size(max = 100, message = "taskName must be at most 100 characters")
    private String taskName;

    @Schema(description = "设备 ID", example = "1")
    @NotNull(message = "deviceId is required")
    private Long deviceId;

    @Schema(description = "产品编码", example = "TEMP_SENSOR_V1")
    @Size(max = 100, message = "productKey must be at most 100 characters")
    private String productKey;

    @Schema(description = "产品名称", example = "温度传感器产品")
    @Size(max = 100, message = "productName must be at most 100 characters")
    private String productName;

    @Schema(description = "采集类型", example = "POLLING")
    @NotBlank(message = "collectionType is required")
    @Size(max = 50, message = "collectionType must be at most 50 characters")
    private String collectionType;

    @Schema(description = "调度类型", example = "CRON")
    @NotBlank(message = "scheduleType is required")
    @Size(max = 50, message = "scheduleType must be at most 50 characters")
    private String scheduleType;

    @Schema(description = "Cron 表达式", example = "0 */5 * * * ?")
    @Size(max = 100, message = "cronExpression must be at most 100 characters")
    private String cronExpression;

    @Schema(description = "轮询间隔秒数", example = "60")
    private Integer pollIntervalSeconds;

    @Schema(description = "数据来源类型", example = "DEVICE_PUSH")
    @Size(max = 50, message = "sourceType must be at most 50 characters")
    private String sourceType;

    @Schema(description = "数据格式", example = "JSON")
    @Size(max = 50, message = "dataFormat must be at most 50 characters")
    private String dataFormat;

    @Schema(description = "采集配置 JSON")
    private String configJson;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "enabled is required")
    private Boolean enabled;

    @Schema(description = "任务描述", example = "每 5 分钟采集一次温度值")
    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;
}
