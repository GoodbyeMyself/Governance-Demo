package com.governance.datasource.dto;

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
public class DataSourceStatsOverviewResponse {

    private Long totalDataSources;
    private Long databaseDataSourceCount;
    private Long fileSystemDataSourceCount;
    private List<DailyTrendItem> dataSourceUpdateTrend7d;
    private List<RecentDataSourceItem> recentDataSources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private LocalDate date;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentDataSourceItem {
        private Long id;
        private String name;
        private String type;
        private LocalDateTime updatedAt;
    }
}
