package com.governance.platform.modules.workbench.dto;

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
public class WorkbenchOverviewResponse {

    private Long totalDataSources;
    private Long databaseDataSourceCount;
    private Long fileSystemDataSourceCount;

    private Long totalMetadataTasks;
    private Long enabledMetadataTaskCount;
    private Long cronMetadataTaskCount;
    private Long fullMetadataTaskCount;
    private Long incrementalMetadataTaskCount;

    private List<DailyTrendItem> dataSourceUpdateTrend7d;
    private List<DailyTrendItem> metadataTaskUpdateTrend7d;

    private List<RecentDataSourceItem> recentDataSources;
    private List<RecentMetadataTaskItem> recentMetadataTasks;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentMetadataTaskItem {
        private Long id;
        private String taskName;
        private String dataSourceName;
        private String strategy;
        private String scheduleType;
        private Boolean enabled;
        private LocalDateTime updatedAt;
    }
}
