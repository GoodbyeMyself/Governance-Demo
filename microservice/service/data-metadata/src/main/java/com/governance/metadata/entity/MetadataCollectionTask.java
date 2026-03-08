package com.governance.metadata.entity;

import com.governance.datasource.entity.DataSourceType;
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
 * 元数据采集任务实体。
 * <p>
 * 该实体记录任务与数据源之间的绑定关系，以及采集策略、
 * 调度方式、扩展配置和启停状态等完整任务信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "metadata_collection_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_metadata_task_name", columnNames = "task_name")
        }
)
public class MetadataCollectionTask {

    /**
     * 任务主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称。
     */
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    /**
     * 关联数据源 ID。
     */
    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId;

    /**
     * 关联数据源名称。
     */
    @Column(name = "data_source_name", nullable = false, length = 100)
    private String dataSourceName;

    /**
     * 关联数据源类型。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source_type", nullable = false, length = 50)
    private DataSourceType dataSourceType;

    /**
     * 采集策略。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_strategy", nullable = false, length = 30)
    private MetadataCollectionStrategy strategy;

    /**
     * 采集范围。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_scope", nullable = false, length = 30)
    private MetadataCollectionScope scope;

    /**
     * 目标匹配模式。
     */
    @Column(name = "target_pattern", length = 500)
    private String targetPattern;

    /**
     * 调度方式。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 30)
    private MetadataCollectionScheduleType scheduleType;

    /**
     * CRON 表达式。
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    /**
     * 扩展配置 JSON。
     */
    @Column(name = "config_json", length = 2000)
    private String configJson;

    /**
     * 任务描述。
     */
    @Column(length = 500)
    private String description;

    /**
     * 是否启用任务。
     */
    @Column(nullable = false)
    private Boolean enabled;

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
