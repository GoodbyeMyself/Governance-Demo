package com.governance.platform.modules.metadata.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskResponse;
import com.governance.platform.modules.datasource.entity.DataSourceInfo;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionTask;
import com.governance.platform.modules.metadata.exception.DuplicateMetadataCollectionTaskException;
import com.governance.platform.shared.exception.ResourceNotFoundException;
import com.governance.platform.modules.datasource.repository.DataSourceRepository;
import com.governance.platform.modules.metadata.repository.MetadataCollectionTaskRepository;
import com.governance.platform.modules.metadata.service.MetadataCollectionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataCollectionTaskServiceImpl implements MetadataCollectionTaskService {

    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;
    private final DataSourceRepository dataSourceRepository;
    private final ObjectMapper objectMapper;

    @Override
    public MetadataCollectionTaskResponse createTask(MetadataCollectionTaskRequest request) {
        String normalizedTaskName = request.getTaskName().trim();
        if (metadataCollectionTaskRepository.existsByTaskName(normalizedTaskName)) {
            throw new DuplicateMetadataCollectionTaskException("Task name already exists: " + normalizedTaskName);
        }

        DataSourceInfo dataSource = getDataSourceById(request.getDataSourceId());

        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        MetadataCollectionTask entity = MetadataCollectionTask.builder()
                .taskName(normalizedTaskName)
                .dataSource(dataSource)
                .strategy(request.getStrategy())
                .scope(request.getScope())
                .targetPattern(trimToNull(request.getTargetPattern()))
                .scheduleType(request.getScheduleType())
                .cronExpression(cronExpression)
                .configJson(configJson)
                .description(trimToNull(request.getDescription()))
                .enabled(request.getEnabled())
                .build();

        MetadataCollectionTask saved = metadataCollectionTaskRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public MetadataCollectionTaskResponse getTaskById(Long id) {
        MetadataCollectionTask task = getTaskEntityById(id);
        return toResponse(task);
    }

    @Override
    public MetadataCollectionTaskResponse updateTask(Long id, MetadataCollectionTaskRequest request) {
        MetadataCollectionTask existing = getTaskEntityById(id);
        String normalizedTaskName = request.getTaskName().trim();

        if (metadataCollectionTaskRepository.existsByTaskNameAndIdNot(normalizedTaskName, id)) {
            throw new DuplicateMetadataCollectionTaskException("Task name already exists: " + normalizedTaskName);
        }

        DataSourceInfo dataSource = getDataSourceById(request.getDataSourceId());
        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        existing.setTaskName(normalizedTaskName);
        existing.setDataSource(dataSource);
        existing.setStrategy(request.getStrategy());
        existing.setScope(request.getScope());
        existing.setTargetPattern(trimToNull(request.getTargetPattern()));
        existing.setScheduleType(request.getScheduleType());
        existing.setCronExpression(cronExpression);
        existing.setConfigJson(configJson);
        existing.setDescription(trimToNull(request.getDescription()));
        existing.setEnabled(request.getEnabled());

        MetadataCollectionTask updated = metadataCollectionTaskRepository.save(existing);
        return toResponse(updated);
    }

    @Override
    public void deleteTask(Long id) {
        MetadataCollectionTask existing = getTaskEntityById(id);
        metadataCollectionTaskRepository.delete(existing);
    }

    @Override
    public List<MetadataCollectionTaskResponse> getAllTasks() {
        return metadataCollectionTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeCronExpression(MetadataCollectionScheduleType scheduleType, String cronExpression) {
        if (scheduleType == MetadataCollectionScheduleType.CRON) {
            if (!StringUtils.hasText(cronExpression)) {
                throw new IllegalArgumentException("cronExpression is required when scheduleType is CRON");
            }
            return cronExpression.trim();
        }
        return null;
    }

    private String normalizeConfigJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return null;
        }

        String normalized = configJson.trim();
        try {
            objectMapper.readTree(normalized);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("configJson must be valid JSON");
        }
        return normalized;
    }

    private DataSourceInfo getDataSourceById(Long dataSourceId) {
        return dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + dataSourceId));
    }

    private MetadataCollectionTask getTaskEntityById(Long id) {
        return metadataCollectionTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metadata collection task not found: " + id));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private MetadataCollectionTaskResponse toResponse(MetadataCollectionTask entity) {
        return MetadataCollectionTaskResponse.builder()
                .id(entity.getId())
                .taskName(entity.getTaskName())
                .dataSourceId(entity.getDataSource().getId())
                .dataSourceName(entity.getDataSource().getName())
                .dataSourceType(entity.getDataSource().getType())
                .strategy(entity.getStrategy())
                .scope(entity.getScope())
                .targetPattern(entity.getTargetPattern())
                .scheduleType(entity.getScheduleType())
                .cronExpression(entity.getCronExpression())
                .configJson(entity.getConfigJson())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}



