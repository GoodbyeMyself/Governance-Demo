package com.governance.iotdevice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备新增或更新请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IoT device create/update request")
public class IotDeviceRequest {

    @Schema(description = "设备编码", example = "DEVICE_001")
    @NotBlank(message = "deviceCode is required")
    @Size(max = 100, message = "deviceCode must be at most 100 characters")
    private String deviceCode;

    @Schema(description = "设备名称", example = "温度传感器-01")
    @NotBlank(message = "deviceName is required")
    @Size(max = 100, message = "deviceName must be at most 100 characters")
    private String deviceName;

    @Schema(description = "产品编码", example = "TEMP_SENSOR_V1")
    @Size(max = 100, message = "productKey must be at most 100 characters")
    private String productKey;

    @Schema(description = "产品名称", example = "温度传感器产品")
    @Size(max = 100, message = "productName must be at most 100 characters")
    private String productName;

    @Schema(description = "设备类型", example = "SENSOR")
    @NotBlank(message = "deviceType is required")
    @Size(max = 50, message = "deviceType must be at most 50 characters")
    private String deviceType;

    @Schema(description = "协议类型", example = "MQTT")
    @NotBlank(message = "protocolType is required")
    @Size(max = 50, message = "protocolType must be at most 50 characters")
    private String protocolType;

    @Schema(description = "连接地址", example = "tcp://127.0.0.1:1883/device/001")
    @Size(max = 500, message = "endpoint must be at most 500 characters")
    private String endpoint;

    @Schema(description = "连接主机", example = "127.0.0.1")
    @Size(max = 255, message = "connectionHost must be at most 255 characters")
    private String connectionHost;

    @Schema(description = "连接端口", example = "1883")
    private Integer connectionPort;

    @Schema(description = "Topic 或路径", example = "/device/001/telemetry")
    @Size(max = 255, message = "topicOrPath must be at most 255 characters")
    private String topicOrPath;

    @Schema(description = "连接用户名", example = "device_user")
    @Size(max = 100, message = "username must be at most 100 characters")
    private String username;

    @Schema(description = "连接密码或密钥", example = "secret")
    @Size(max = 255, message = "passwordOrSecret must be at most 255 characters")
    private String passwordOrSecret;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "在线状态", example = "OFFLINE")
    @NotBlank(message = "onlineStatus is required")
    @Size(max = 50, message = "onlineStatus must be at most 50 characters")
    private String onlineStatus;

    @Schema(description = "设备状态", example = "ENABLED")
    @NotBlank(message = "status is required")
    @Size(max = 50, message = "status must be at most 50 characters")
    private String status;

    @Schema(description = "设备描述", example = "仓库温度监控设备")
    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;
}
