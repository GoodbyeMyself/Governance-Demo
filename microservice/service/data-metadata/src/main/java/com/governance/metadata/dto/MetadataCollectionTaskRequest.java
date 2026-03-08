package com.governance.metadata.dto;

import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionScope;
import com.governance.metadata.entity.MetadataCollectionStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元数据采集任务新增或更新请求参数。
 * <p>
 * 该对象用于采集任务管理页面提交任务配置，
 * 涵盖任务名称、目标数据源、采集策略、调度方式等关键字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "元数据采集任务新增/更新请求参数")
public class MetadataCollectionTaskRequest {

    /**
     * 任务名称。
     */
    @Schema(description = "任务名称", example = "每日订单库全量采集")
    @NotBlank(message = "taskName is required")
    @Size(max = 100, message = "taskName must be at most 100 characters")
    private String taskName;

    /**
     * 关联的数据源 ID。
     */
    @Schema(description = "关联数据源 ID", example = "1")
    @NotNull(message = "dataSourceId is required")
    @Positive(message = "dataSourceId must be positive")
    private Long dataSourceId;

    /**
     * 采集策略。
     */
    @Schema(description = "采集策略", allowableValues = {"FULL", "INCREMENTAL"}, example = "FULL")
    @NotNull(message = "strategy is required")
    private MetadataCollectionStrategy strategy;

    /**
     * 采集范围。
     */
    @Schema(description = "采集范围", allowableValues = {"SCHEMA", "TABLE"}, example = "TABLE")
    @NotNull(message = "scope is required")
    private MetadataCollectionScope scope;

    /**
     * 目标匹配模式。
     */
    @Schema(description = "目标匹配模式（如库名、表名前缀）", example = "order_*")
    @Size(max = 500, message = "targetPattern must be at most 500 characters")
    private String targetPattern;

    /**
     * 调度方式。
     */
    @Schema(description = "调度方式", allowableValues = {"MANUAL", "CRON"}, example = "CRON")
    @NotNull(message = "scheduleType is required")
    private MetadataCollectionScheduleType scheduleType;

    /**
     * CRON 表达式。
     */
    @Schema(description = "CRON 表达式（调度方式为 CRON 时填写）", example = "0 0/30 * * * ?")
    @Size(max = 100, message = "cronExpression must be at most 100 characters")
    private String cronExpression;

    /**
     * 任务扩展配置 JSON。
     */
    @Schema(description = "任务扩展配置 JSON", example = "{\"parallelism\":2}")
    @Size(max = 2000, message = "configJson must be at most 2000 characters")
    private String configJson;

    /**
     * 任务描述。
     */
    @Schema(description = "任务描述", example = "订单域核心表定时采集任务")
    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    /**
     * 是否启用任务。
     */
    @Schema(description = "是否启用任务", example = "true")
    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
