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
 * IoT 采集任务实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "iot_collection_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_iot_collection_task_name", columnNames = "task_name")
        }
)
public class IotCollectionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

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

    @Column(name = "collection_type", nullable = false, length = 50)
    private String collectionType;

    @Column(name = "schedule_type", nullable = false, length = 50)
    private String scheduleType;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "poll_interval_seconds")
    private Integer pollIntervalSeconds;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "data_format", length = 50)
    private String dataFormat;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
