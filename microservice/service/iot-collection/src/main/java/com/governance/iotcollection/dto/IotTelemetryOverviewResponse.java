package com.governance.iotcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 实时数据概览响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotTelemetryOverviewResponse {

    private Long totalTelemetryDevices;
    private Long totalTelemetryRecords;
    private Long latestTelemetryCount;
    private List<DailyTrendItem> telemetryTrend7d;
    private List<RecentTelemetryItem> recentTelemetryItems;

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
    public static class RecentTelemetryItem {
        private Long id;
        private Long deviceId;
        private String deviceName;
        private String metricCode;
        private String metricName;
        private String metricValue;
        private LocalDateTime updatedAt;
    }
}
