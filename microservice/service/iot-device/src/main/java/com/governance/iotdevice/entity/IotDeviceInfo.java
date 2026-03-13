package com.governance.iotdevice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * IoT 设备实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "iot_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_iot_device_code", columnNames = "device_code")
        }
)
public class IotDeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", nullable = false, length = 100)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "product_key", length = 100)
    private String productKey;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    @Column(name = "protocol_type", nullable = false, length = 50)
    private String protocolType;

    @Column(length = 500)
    private String endpoint;

    @Column(name = "connection_host", length = 255)
    private String connectionHost;

    @Column(name = "connection_port")
    private Integer connectionPort;

    @Column(name = "topic_or_path", length = 255)
    private String topicOrPath;

    @Column(length = 100)
    private String username;

    @Column(name = "password_or_secret", length = 255)
    private String passwordOrSecret;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "online_status", nullable = false, length = 50)
    private String onlineStatus;

    @Column(name = "last_online_at")
    private LocalDateTime lastOnlineAt;

    @Column(name = "last_offline_at")
    private LocalDateTime lastOfflineAt;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
