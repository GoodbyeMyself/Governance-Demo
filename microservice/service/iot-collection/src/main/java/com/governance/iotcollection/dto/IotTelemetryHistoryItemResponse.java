package com.governance.iotcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 历史实时数据响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotTelemetryHistoryItemResponse {

    private Long id;
    private Long deviceId;
    private Long taskId;
    private String deviceCode;
    private String deviceName;
    private String productKey;
    private String productName;
    private String metricCode;
    private String metricName;
    private String metricValue;
    private String valueType;
    private String unit;
    private String quality;
    private LocalDateTime collectedAt;
    private LocalDateTime receivedAt;
    private String sourceType;
    private String dataFormat;
    private String payloadJson;
    private LocalDateTime createdAt;
}
