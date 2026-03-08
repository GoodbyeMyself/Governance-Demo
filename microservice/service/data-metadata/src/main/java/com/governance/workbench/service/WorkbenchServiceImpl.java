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

/**
 * 工作台服务实现。
 *
 * <p>负责聚合数据源服务与元数据服务自身的数据，
 * 生成前端首页直接可消费的概览模型。</p>
 */
@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl implements WorkbenchService {

    private final DataSourceClient dataSourceClient;
    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    /**
     * 组装工作台首页概览数据。
     *
     * @return 工作台概览
     */
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

    /**
     * 查询数据源概览。
     *
     * <p>如果远程调用失败，则返回空概览，避免首页完全不可用。</p>
     *
     * @return 数据源概览
     */
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

    /**
     * 构建最近 7 天的元数据任务更新趋势。
     *
     * @return 按天聚合的趋势列表
     */
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

    /**
     * 把任务实体转换为工作台最近任务条目。
     *
     * @param entity 任务实体
     * @return 最近任务条目
     */
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
