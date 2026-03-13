package com.governance.iotdevice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT device response")
public class IotDeviceResponse {

    @Schema(description = "设备 ID", example = "1")
    private Long id;

    @Schema(description = "设备编码", example = "DEVICE_001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "温度传感器-01")
    private String deviceName;

    @Schema(description = "产品编码", example = "TEMP_SENSOR_V1")
    private String productKey;

    @Schema(description = "产品名称", example = "温度传感器产品")
    private String productName;

    @Schema(description = "设备类型", example = "SENSOR")
    private String deviceType;

    @Schema(description = "协议类型", example = "MQTT")
    private String protocolType;

    @Schema(description = "连接地址")
    private String endpoint;

    @Schema(description = "连接主机")
    private String connectionHost;

    @Schema(description = "连接端口")
    private Integer connectionPort;

    @Schema(description = "Topic 或路径")
    private String topicOrPath;

    @Schema(description = "连接用户名")
    private String username;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "在线状态", example = "OFFLINE")
    private String onlineStatus;

    @Schema(description = "最近上线时间")
    private LocalDateTime lastOnlineAt;

    @Schema(description = "最近下线时间")
    private LocalDateTime lastOfflineAt;

    @Schema(description = "设备状态", example = "ENABLED")
    private String status;

    @Schema(description = "设备描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
