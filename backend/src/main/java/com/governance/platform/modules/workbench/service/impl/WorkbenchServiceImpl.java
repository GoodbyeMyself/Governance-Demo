package com.governance.platform.modules.workbench.service.impl;

import com.governance.platform.modules.datasource.entity.DataSourceInfo;
import com.governance.platform.modules.datasource.entity.DataSourceType;
import com.governance.platform.modules.datasource.repository.DataSourceRepository;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionStrategy;
import com.governance.platform.modules.metadata.entity.MetadataCollectionTask;
import com.governance.platform.modules.metadata.repository.MetadataCollectionTaskRepository;
import com.governance.platform.modules.workbench.dto.WorkbenchOverviewResponse;
import com.governance.platform.modules.workbench.service.WorkbenchService;
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

    private final DataSourceRepository dataSourceRepository;
    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    @Override
    public WorkbenchOverviewResponse getOverview() {
        long totalDataSources = dataSourceRepository.count();
        long databaseDataSourceCount = dataSourceRepository.countByType(DataSourceType.DATABASE);
        long fileSystemDataSourceCount = dataSourceRepository.countByType(DataSourceType.FILE_SYSTEM);

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

        List<WorkbenchOverviewResponse.DailyTrendItem> dataSourceUpdateTrend7d = buildDataSourceTrend7d();
        List<WorkbenchOverviewResponse.DailyTrendItem> metadataTaskUpdateTrend7d = buildMetadataTaskTrend7d();

        List<WorkbenchOverviewResponse.RecentDataSourceItem> recentDataSources = dataSourceRepository
                .findTop5ByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::toRecentDataSourceItem)
                .toList();

        List<WorkbenchOverviewResponse.RecentMetadataTaskItem> recentMetadataTasks = metadataCollectionTaskRepository
                .findTop5ByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::toRecentMetadataTaskItem)
                .toList();

        return WorkbenchOverviewResponse.builder()
                .totalDataSources(totalDataSources)
                .databaseDataSourceCount(databaseDataSourceCount)
                .fileSystemDataSourceCount(fileSystemDataSourceCount)
                .totalMetadataTasks(totalMetadataTasks)
                .enabledMetadataTaskCount(enabledMetadataTaskCount)
                .cronMetadataTaskCount(cronMetadataTaskCount)
                .fullMetadataTaskCount(fullMetadataTaskCount)
                .incrementalMetadataTaskCount(incrementalMetadataTaskCount)
                .dataSourceUpdateTrend7d(dataSourceUpdateTrend7d)
                .metadataTaskUpdateTrend7d(metadataTaskUpdateTrend7d)
                .recentDataSources(recentDataSources)
                .recentMetadataTasks(recentMetadataTasks)
                .build();
    }

    private List<WorkbenchOverviewResponse.DailyTrendItem> buildDataSourceTrend7d() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<DataSourceInfo> items = dataSourceRepository.findByUpdatedAtBetween(start, end);
        Map<LocalDate, Long> countMap = new HashMap<>();
        for (DataSourceInfo item : items) {
            LocalDate day = item.getUpdatedAt().toLocalDate();
            countMap.put(day, countMap.getOrDefault(day, 0L) + 1L);
        }
        return buildTrendItems(today, countMap);
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
        return buildTrendItems(today, countMap);
    }

    private List<WorkbenchOverviewResponse.DailyTrendItem> buildTrendItems(
            LocalDate today,
            Map<LocalDate, Long> countMap
    ) {
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

    private WorkbenchOverviewResponse.RecentDataSourceItem toRecentDataSourceItem(DataSourceInfo entity) {
        return WorkbenchOverviewResponse.RecentDataSourceItem.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType().name())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private WorkbenchOverviewResponse.RecentMetadataTaskItem toRecentMetadataTaskItem(MetadataCollectionTask entity) {
        return WorkbenchOverviewResponse.RecentMetadataTaskItem.builder()
                .id(entity.getId())
                .taskName(entity.getTaskName())
                .dataSourceName(entity.getDataSource().getName())
                .strategy(entity.getStrategy().name())
                .scheduleType(entity.getScheduleType().name())
                .enabled(entity.getEnabled())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
