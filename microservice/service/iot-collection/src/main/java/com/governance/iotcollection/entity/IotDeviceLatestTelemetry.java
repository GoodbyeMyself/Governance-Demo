package com.governance.iotcollection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * IoT 设备最新实时数据实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "iot_device_latest_telemetry",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_iot_device_latest_metric", columnNames = {"device_id", "metric_code"})
        }
)
public class IotDeviceLatestTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

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

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
