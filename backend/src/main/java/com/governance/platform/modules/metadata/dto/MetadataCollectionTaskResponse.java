package com.governance.platform.modules.metadata.dto;

import com.governance.platform.modules.datasource.entity.DataSourceType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScope;
import com.governance.platform.modules.metadata.entity.MetadataCollectionStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "元数据采集任务响应结果")
public class MetadataCollectionTaskResponse {
    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "任务名称", example = "每日订单库全量采集")
    private String taskName;

    @Schema(description = "数据源ID", example = "1")
    private Long dataSourceId;

    @Schema(description = "数据源名称", example = "生产库-订单中心")
    private String dataSourceName;

    @Schema(description = "数据源类型", allowableValues = {"DATABASE", "FILE_SYSTEM"}, example = "DATABASE")
    private DataSourceType dataSourceType;

    @Schema(description = "采集策略", allowableValues = {"FULL", "INCREMENTAL"}, example = "FULL")
    private MetadataCollectionStrategy strategy;

    @Schema(description = "采集范围", allowableValues = {"SCHEMA", "TABLE"}, example = "TABLE")
    private MetadataCollectionScope scope;

    @Schema(description = "目标匹配模式")
    private String targetPattern;

    @Schema(description = "调度方式", allowableValues = {"MANUAL", "CRON"}, example = "CRON")
    private MetadataCollectionScheduleType scheduleType;

    @Schema(description = "CRON 表达式")
    private String cronExpression;

    @Schema(description = "任务扩展配置 JSON")
    private String configJson;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}



