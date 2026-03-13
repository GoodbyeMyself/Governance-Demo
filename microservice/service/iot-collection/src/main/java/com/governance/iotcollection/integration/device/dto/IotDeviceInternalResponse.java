package com.governance.iotcollection.integration.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备服务内部响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceInternalResponse {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String productKey;
    private String productName;
    private String deviceType;
    private String protocolType;
    private String endpoint;
    private String connectionHost;
    private Integer connectionPort;
    private String topicOrPath;
    private String username;
    private Boolean enabled;
    private String onlineStatus;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
