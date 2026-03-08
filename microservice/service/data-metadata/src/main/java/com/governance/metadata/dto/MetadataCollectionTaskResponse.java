package com.governance.metadata.dto;

import com.governance.datasource.entity.DataSourceType;
import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionScope;
import com.governance.metadata.entity.MetadataCollectionStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 元数据采集任务响应对象。
 * <p>
 * 对外返回任务配置详情、关联数据源信息和审计字段，
 * 供任务列表、详情和工作台复用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "元数据采集任务响应结果")
public class MetadataCollectionTaskResponse {

    /**
     * 任务 ID。
     */
    @Schema(description = "任务 ID", example = "1")
    private Long id;

    /**
     * 任务名称。
     */
    @Schema(description = "任务名称", example = "每日订单库全量采集")
    private String taskName;

    /**
     * 数据源 ID。
     */
    @Schema(description = "数据源 ID", example = "1")
    private Long dataSourceId;

    /**
     * 数据源名称。
     */
    @Schema(description = "数据源名称", example = "生产库-订单中心")
    private String dataSourceName;

    /**
     * 数据源类型。
     */
    @Schema(description = "数据源类型", allowableValues = {"DATABASE", "FILE_SYSTEM"}, example = "DATABASE")
    private DataSourceType dataSourceType;

    /**
     * 采集策略。
     */
    @Schema(description = "采集策略", allowableValues = {"FULL", "INCREMENTAL"}, example = "FULL")
    private MetadataCollectionStrategy strategy;

    /**
     * 采集范围。
     */
    @Schema(description = "采集范围", allowableValues = {"SCHEMA", "TABLE"}, example = "TABLE")
    private MetadataCollectionScope scope;

    /**
     * 目标匹配模式。
     */
    @Schema(description = "目标匹配模式")
    private String targetPattern;

    /**
     * 调度方式。
     */
    @Schema(description = "调度方式", allowableValues = {"MANUAL", "CRON"}, example = "CRON")
    private MetadataCollectionScheduleType scheduleType;

    /**
     * CRON 表达式。
     */
    @Schema(description = "CRON 表达式")
    private String cronExpression;

    /**
     * 扩展配置 JSON。
     */
    @Schema(description = "任务扩展配置 JSON")
    private String configJson;

    /**
     * 任务描述。
     */
    @Schema(description = "任务描述")
    private String description;

    /**
     * 是否启用。
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
