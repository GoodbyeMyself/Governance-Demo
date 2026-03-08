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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId;

    @Column(name = "data_source_name", nullable = false, length = 100)
    private String dataSourceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source_type", nullable = false, length = 50)
    private DataSourceType dataSourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_strategy", nullable = false, length = 30)
    private MetadataCollectionStrategy strategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_scope", nullable = false, length = 30)
    private MetadataCollectionScope scope;

    @Column(name = "target_pattern", length = 500)
    private String targetPattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 30)
    private MetadataCollectionScheduleType scheduleType;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "config_json", length = 2000)
    private String configJson;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
