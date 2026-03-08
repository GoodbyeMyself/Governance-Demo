package com.governance.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 数据源实体。
 * <p>
 * 该实体映射平台维护的数据源表，记录数据源类型、连接信息、
 * 描述以及创建更新时间，供元数据采集任务关联使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "data_sources",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_data_source_name", columnNames = "name")
        }
)
public class DataSourceInfo {

    /**
     * 数据源主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据源名称。
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 数据源类型。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DataSourceType type;

    /**
     * 连接地址。
     */
    @Column(name = "connection_url", length = 500)
    private String connectionUrl;

    /**
     * 连接用户名。
     */
    @Column(length = 100)
    private String username;

    /**
     * 连接密码。
     */
    @Column(length = 100)
    private String password;

    /**
     * 数据源描述。
     */
    @Column(length = 500)
    private String description;

    /**
     * 创建时间。
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
