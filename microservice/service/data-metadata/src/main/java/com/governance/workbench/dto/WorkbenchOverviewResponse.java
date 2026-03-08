package com.governance.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作台概览响应结果")
public class WorkbenchOverviewResponse {

    @Schema(description = "数据源总数", example = "12")
    private Long totalDataSources;

    @Schema(description = "数据库类型数据源数量", example = "10")
    private Long databaseDataSourceCount;

    @Schema(description = "文件系统类型数据源数量", example = "2")
    private Long fileSystemDataSourceCount;

    @Schema(description = "采集任务总数", example = "20")
    private Long totalMetadataTasks;

    @Schema(description = "已启用采集任务数量", example = "15")
    private Long enabledMetadataTaskCount;

    @Schema(description = "CRON 调度任务数量", example = "8")
    private Long cronMetadataTaskCount;

    @Schema(description = "全量任务数量", example = "9")
    private Long fullMetadataTaskCount;

    @Schema(description = "增量任务数量", example = "11")
    private Long incrementalMetadataTaskCount;

    @Schema(description = "近7天数据源变更趋势")
    private List<DailyTrendItem> dataSourceUpdateTrend7d;

    @Schema(description = "近7天采集任务变更趋势")
    private List<DailyTrendItem> metadataTaskUpdateTrend7d;

    @Schema(description = "最近更新的数据源列表")
    private List<RecentDataSourceItem> recentDataSources;

    @Schema(description = "最近更新的采集任务列表")
    private List<RecentMetadataTaskItem> recentMetadataTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日趋势项")
    public static class DailyTrendItem {
        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "数量", example = "3")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近更新数据源项")
    public static class RecentDataSourceItem {
        @Schema(description = "数据源ID", example = "1")
        private Long id;

        @Schema(description = "数据源名称", example = "生产库-订单中心")
        private String name;

        @Schema(description = "数据源类型", example = "DATABASE")
        private String type;

        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近更新采集任务项")
    public static class RecentMetadataTaskItem {
        @Schema(description = "任务ID", example = "1")
        private Long id;

        @Schema(description = "任务名称", example = "每日订单库全量采集")
        private String taskName;

        @Schema(description = "数据源名称", example = "生产库-订单中心")
        private String dataSourceName;

        @Schema(description = "采集策略", example = "FULL")
        private String strategy;

        @Schema(description = "调度方式", example = "CRON")
        private String scheduleType;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }
}
