package com.governance.workbench.service;

import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionStrategy;
import com.governance.metadata.entity.MetadataCollectionTask;
import com.governance.metadata.integration.datasource.DataSourceClient;
import com.governance.metadata.integration.datasource.dto.DataSourceStatsOverviewResponse;
import com.governance.metadata.repository.MetadataCollectionTaskRepository;
import com.governance.workbench.dto.WorkbenchOverviewResponse;
import com.governance.workbench.service.WorkbenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl implements WorkbenchService {

    private final DataSourceClient dataSourceClient;
    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    @Override
    public WorkbenchOverviewResponse getOverview() {
        DataSourceStatsOverviewResponse dataSourceOverview = getDataSourceOverview();

        long totalMetadataTasks = metadataCollectionTaskRepository.count();
        long enabledMetadataTaskCount = metadataCollectionTaskRepository.countByEnabledTrue();
        long cronMetadataTaskCount = metadataCollectionTaskRepository.countByScheduleType(
                MetadataCollectionScheduleType.CRON
        );
        long fullMetadataTaskCount = metadataCollectionTaskRepository.countByStrategy(
                MetadataCollectionStrategy.FULL
        );
        long incrementalMetadataTaskCount = metadataCollectionTaskRepository.countByStrategy(
                MetadataCollectionStrategy.INCREMENTAL
        );

        List<WorkbenchOverviewResponse.DailyTrendItem> metadataTaskUpdateTrend7d = buildMetadataTaskTrend7d();

        List<WorkbenchOverviewResponse.RecentMetadataTaskItem> recentMetadataTasks = metadataCollectionTaskRepository
                .findTop5ByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::toRecentMetadataTaskItem)
                .toList();

        List<WorkbenchOverviewResponse.DailyTrendItem> dataSourceTrend = dataSourceOverview
                .getDataSourceUpdateTrend7d()
                .stream()
                .map(item -> WorkbenchOverviewResponse.DailyTrendItem.builder()
                        .date(item.getDate())
                        .count(item.getCount())
                        .build())
                .toList();

        List<WorkbenchOverviewResponse.RecentDataSourceItem> recentDataSources = dataSourceOverview
                .getRecentDataSources()
                .stream()
                .map(item -> WorkbenchOverviewResponse.RecentDataSourceItem.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .type(item.getType())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();

        return WorkbenchOverviewResponse.builder()
                .totalDataSources(dataSourceOverview.getTotalDataSources())
                .databaseDataSourceCount(dataSourceOverview.getDatabaseDataSourceCount())
                .fileSystemDataSourceCount(dataSourceOverview.getFileSystemDataSourceCount())
                .totalMetadataTasks(totalMetadataTasks)
                .enabledMetadataTaskCount(enabledMetadataTaskCount)
                .cronMetadataTaskCount(cronMetadataTaskCount)
                .fullMetadataTaskCount(fullMetadataTaskCount)
                .incrementalMetadataTaskCount(incrementalMetadataTaskCount)
                .dataSourceUpdateTrend7d(dataSourceTrend)
                .metadataTaskUpdateTrend7d(metadataTaskUpdateTrend7d)
                .recentDataSources(recentDataSources)
                .recentMetadataTasks(recentMetadataTasks)
                .build();
    }

    private DataSourceStatsOverviewResponse getDataSourceOverview() {
        try {
            DataSourceStatsOverviewResponse overview = dataSourceClient.getOverview();
            if (overview == null) {
                return DataSourceStatsOverviewResponse.builder()
                        .totalDataSources(0L)
                        .databaseDataSourceCount(0L)
                        .fileSystemDataSourceCount(0L)
                        .dataSourceUpdateTrend7d(List.of())
                        .recentDataSources(List.of())
                        .build();
            }
            return overview;
        } catch (Exception ex) {
            return DataSourceStatsOverviewResponse.builder()
                    .totalDataSources(0L)
                    .databaseDataSourceCount(0L)
                    .fileSystemDataSourceCount(0L)
                    .dataSourceUpdateTrend7d(List.of())
                    .recentDataSources(List.of())
                    .build();
        }
    }

    private List<WorkbenchOverviewResponse.DailyTrendItem> buildMetadataTaskTrend7d() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<MetadataCollectionTask> items = metadataCollectionTaskRepository.findByUpdatedAtBetween(start, end);
        Map<LocalDate, Long> countMap = new HashMap<>();
        for (MetadataCollectionTask item : items) {
            LocalDate day = item.getUpdatedAt().toLocalDate();
            countMap.put(day, countMap.getOrDefault(day, 0L) + 1L);
        }

        return java.util.stream.LongStream.rangeClosed(0, 6)
                .mapToObj(offset -> {
                    LocalDate date = today.minusDays(6 - offset);
                    return WorkbenchOverviewResponse.DailyTrendItem.builder()
                            .date(date)
                            .count(countMap.getOrDefault(date, 0L))
                            .build();
                })
                .toList();
    }

    private WorkbenchOverviewResponse.RecentMetadataTaskItem toRecentMetadataTaskItem(MetadataCollectionTask entity) {
        return WorkbenchOverviewResponse.RecentMetadataTaskItem.builder()
                .id(entity.getId())
                .taskName(entity.getTaskName())
                .dataSourceName(entity.getDataSourceName())
                .strategy(entity.getStrategy().name())
                .scheduleType(entity.getScheduleType().name())
                .enabled(entity.getEnabled())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
