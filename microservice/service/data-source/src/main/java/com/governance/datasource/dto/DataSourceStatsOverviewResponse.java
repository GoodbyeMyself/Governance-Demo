package com.governance.datasource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源统计概览响应对象。
 * <p>
 * 该对象用于仪表盘或工作台展示数据源总量、分类统计、
 * 近期变更趋势以及最近更新的数据源列表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceStatsOverviewResponse {

    /**
     * 数据源总数。
     */
    private Long totalDataSources;

    /**
     * 数据库类型数据源数量。
     */
    private Long databaseDataSourceCount;

    /**
     * 文件系统类型数据源数量。
     */
    private Long fileSystemDataSourceCount;

    /**
     * 最近 7 天数据源更新趋势。
     */
    private List<DailyTrendItem> dataSourceUpdateTrend7d;

    /**
     * 最近更新的数据源列表。
     */
    private List<RecentDataSourceItem> recentDataSources;

    /**
     * 每日趋势项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        /**
         * 统计日期。
         */
        private LocalDate date;

        /**
         * 当日数量。
         */
        private Long count;
    }

    /**
     * 最近数据源项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentDataSourceItem {
        /**
         * 数据源 ID。
         */
        private Long id;

        /**
         * 数据源名称。
         */
        private String name;

        /**
         * 数据源类型。
         */
        private String type;

        /**
         * 最近更新时间。
         */
        private LocalDateTime updatedAt;
    }
}
