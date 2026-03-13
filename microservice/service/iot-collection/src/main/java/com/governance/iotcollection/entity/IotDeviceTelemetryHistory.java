package com.governance.iotcollection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * IoT 设备历史实时数据实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "iot_device_telemetry_history")
public class IotDeviceTelemetryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "device_code", nullable = false, length = 100)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "product_key", length = 100)
    private String productKey;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "metric_code", nullable = false, length = 100)
    private String metricCode;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false, length = 255)
    private String metricValue;

    @Column(name = "value_type", length = 50)
    private String valueType;

    @Column(length = 50)
    private String unit;

    @Column(length = 50)
    private String quality;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "data_format", length = 50)
    private String dataFormat;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
