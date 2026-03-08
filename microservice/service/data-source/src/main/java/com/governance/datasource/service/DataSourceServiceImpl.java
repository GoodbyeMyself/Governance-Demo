package com.governance.datasource.service;

import com.governance.datasource.dto.DataSourceRequest;
import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.dto.DataSourceStatsOverviewResponse;
import com.governance.datasource.entity.DataSourceInfo;
import com.governance.datasource.entity.DataSourceType;
import com.governance.datasource.exception.DataSourceInUseException;
import com.governance.datasource.exception.DuplicateDataSourceException;
import com.governance.datasource.integration.metadata.MetadataTaskClient;
import com.governance.datasource.repository.DataSourceRepository;
import com.governance.datasource.service.DataSourceService;
import com.governance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源服务实现。
 *
 * <p>负责处理数据源 CRUD、引用校验以及首页概览统计。</p>
 */
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final MetadataTaskClient metadataTaskClient;

    /**
     * 创建数据源。
     *
     * @param request 数据源请求
     * @return 创建后的数据源
     */
    @Override
    public DataSourceResponse createDataSource(DataSourceRequest request) {
        if (dataSourceRepository.existsByName(request.getName())) {
            throw new DuplicateDataSourceException("Data source name already exists: " + request.getName());
        }

        DataSourceInfo entity = DataSourceInfo.builder()
                .name(request.getName())
                .type(request.getType())
                .connectionUrl(request.getConnectionUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .description(request.getDescription())
                .build();

        DataSourceInfo saved = dataSourceRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * 删除数据源。
     *
     * <p>删除前会先检查该数据源是否仍被采集任务引用。</p>
     *
     * @param id 数据源 ID
     */
    @Override
    public void deleteDataSource(Long id) {
        DataSourceInfo existing = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));

        long referenceCount = getTaskReferenceCount(id);
        if (referenceCount > 0) {
            throw new DataSourceInUseException(
                    "Data source is referenced by metadata collection tasks: " + id
            );
        }

        dataSourceRepository.delete(existing);
    }

    /**
     * 更新数据源。
     *
     * @param id 数据源 ID
     * @param request 更新请求
     * @return 更新后的数据源
     */
    @Override
    public DataSourceResponse updateDataSource(Long id, DataSourceRequest request) {
        DataSourceInfo existing = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));

        if (dataSourceRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateDataSourceException("Data source name already exists: " + request.getName());
        }

        existing.setName(request.getName());
        existing.setType(request.getType());
        existing.setConnectionUrl(request.getConnectionUrl());
        existing.setUsername(request.getUsername());
        existing.setPassword(request.getPassword());
        existing.setDescription(request.getDescription());

        DataSourceInfo updated = dataSourceRepository.save(existing);
        return toResponse(updated);
    }

    /**
     * 查询全部数据源。
     *
     * @return 数据源列表
     */
    @Override
    public List<DataSourceResponse> getAllDataSources() {
        return dataSourceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 按 ID 查询数据源详情。
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    @Override
    public DataSourceResponse getDataSourceById(Long id) {
        DataSourceInfo entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));
        return toResponse(entity);
    }

    /**
     * 统计数据源概览。
     *
     * <p>用于门户与后台工作台首页展示。</p>
     *
     * @return 数据源概览
     */
    @Override
    public DataSourceStatsOverviewResponse getOverview() {
        long totalDataSources = dataSourceRepository.count();
        long databaseDataSourceCount = dataSourceRepository.countByType(DataSourceType.DATABASE);
        long fileSystemDataSourceCount = dataSourceRepository.countByType(DataSourceType.FILE_SYSTEM);

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

        List<DataSourceStatsOverviewResponse.DailyTrendItem> trendItems = java.util.stream.LongStream.rangeClosed(0, 6)
                .mapToObj(offset -> {
                    LocalDate date = today.minusDays(6 - offset);
                    return DataSourceStatsOverviewResponse.DailyTrendItem.builder()
                            .date(date)
                            .count(countMap.getOrDefault(date, 0L))
                            .build();
                })
                .toList();

        List<DataSourceStatsOverviewResponse.RecentDataSourceItem> recentDataSources = dataSourceRepository
                .findTop5ByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(entity -> DataSourceStatsOverviewResponse.RecentDataSourceItem.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .type(entity.getType().name())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .toList();

        return DataSourceStatsOverviewResponse.builder()
                .totalDataSources(totalDataSources)
                .databaseDataSourceCount(databaseDataSourceCount)
                .fileSystemDataSourceCount(fileSystemDataSourceCount)
                .dataSourceUpdateTrend7d(trendItems)
                .recentDataSources(recentDataSources)
                .build();
    }

    /**
     * 查询某个数据源被采集任务引用的次数。
     *
     * @param dataSourceId 数据源 ID
     * @return 引用次数
     */
    private long getTaskReferenceCount(Long dataSourceId) {
        try {
            Long count = metadataTaskClient.countByDataSourceId(dataSourceId);
            return count == null ? 0L : count;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify metadata task references from data-metadata service");
        }
    }

    /**
     * 实体转响应对象。
     *
     * @param entity 数据源实体
     * @return 数据源响应
     */
    private DataSourceResponse toResponse(DataSourceInfo entity) {
        return DataSourceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .connectionUrl(entity.getConnectionUrl())
                .username(entity.getUsername())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
