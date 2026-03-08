package com.governance.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作台概览响应对象。
 * <p>
 * 该对象用于聚合展示数据源与元数据采集任务的整体运行概况，
 * 是门户首页和治理工作台的核心统计模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作台概览响应结果")
public class WorkbenchOverviewResponse {

    /**
     * 数据源总数。
     */
    @Schema(description = "数据源总数", example = "12")
    private Long totalDataSources;

    /**
     * 数据库类型数据源数量。
     */
    @Schema(description = "数据库类型数据源数量", example = "10")
    private Long databaseDataSourceCount;

    /**
     * 文件系统类型数据源数量。
     */
    @Schema(description = "文件系统类型数据源数量", example = "2")
    private Long fileSystemDataSourceCount;

    /**
     * 采集任务总数。
     */
    @Schema(description = "采集任务总数", example = "20")
    private Long totalMetadataTasks;

    /**
     * 已启用采集任务数量。
     */
    @Schema(description = "已启用采集任务数量", example = "15")
    private Long enabledMetadataTaskCount;

    /**
     * CRON 调度任务数量。
     */
    @Schema(description = "CRON 调度任务数量", example = "8")
    private Long cronMetadataTaskCount;

    /**
     * 全量采集任务数量。
     */
    @Schema(description = "全量任务数量", example = "9")
    private Long fullMetadataTaskCount;

    /**
     * 增量采集任务数量。
     */
    @Schema(description = "增量任务数量", example = "11")
    private Long incrementalMetadataTaskCount;

    /**
     * 最近 7 天数据源更新趋势。
     */
    @Schema(description = "最近 7 天数据源变更趋势")
    private List<DailyTrendItem> dataSourceUpdateTrend7d;

    /**
     * 最近 7 天采集任务更新趋势。
     */
    @Schema(description = "最近 7 天采集任务变更趋势")
    private List<DailyTrendItem> metadataTaskUpdateTrend7d;

    /**
     * 最近更新的数据源列表。
     */
    @Schema(description = "最近更新的数据源列表")
    private List<RecentDataSourceItem> recentDataSources;

    /**
     * 最近更新的采集任务列表。
     */
    @Schema(description = "最近更新的采集任务列表")
    private List<RecentMetadataTaskItem> recentMetadataTasks;

    /**
     * 每日趋势项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日趋势项")
    public static class DailyTrendItem {
        /**
         * 日期。
         */
        @Schema(description = "日期")
        private LocalDate date;

        /**
         * 数量。
         */
        @Schema(description = "数量", example = "3")
        private Long count;
    }

    /**
     * 最近更新数据源项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近更新数据源项")
    public static class RecentDataSourceItem {
        /**
         * 数据源 ID。
         */
        @Schema(description = "数据源 ID", example = "1")
        private Long id;

        /**
         * 数据源名称。
         */
        @Schema(description = "数据源名称", example = "生产库-订单中心")
        private String name;

        /**
         * 数据源类型。
         */
        @Schema(description = "数据源类型", example = "DATABASE")
        private String type;

        /**
         * 最近更新时间。
         */
        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }

    /**
     * 最近更新采集任务项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近更新采集任务项")
    public static class RecentMetadataTaskItem {
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
         * 数据源名称。
         */
        @Schema(description = "数据源名称", example = "生产库-订单中心")
        private String dataSourceName;

        /**
         * 采集策略。
         */
        @Schema(description = "采集策略", example = "FULL")
        private String strategy;

        /**
         * 调度方式。
         */
        @Schema(description = "调度方式", example = "CRON")
        private String scheduleType;

        /**
         * 是否启用。
         */
        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        /**
         * 最近更新时间。
         */
        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }
}
