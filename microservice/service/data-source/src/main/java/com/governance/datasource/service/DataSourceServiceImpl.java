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

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final MetadataTaskClient metadataTaskClient;

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

    @Override
    public List<DataSourceResponse> getAllDataSources() {
        return dataSourceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DataSourceResponse getDataSourceById(Long id) {
        DataSourceInfo entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));
        return toResponse(entity);
    }

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

    private long getTaskReferenceCount(Long dataSourceId) {
        try {
            Long count = metadataTaskClient.countByDataSourceId(dataSourceId);
            return count == null ? 0L : count;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify metadata task references from data-metadata service");
        }
    }

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
