package com.governance.metadata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.governance.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.metadata.dto.MetadataCollectionTaskResponse;
import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionTask;
import com.governance.metadata.exception.DuplicateMetadataCollectionTaskException;
import com.governance.metadata.integration.datasource.DataSourceClient;
import com.governance.metadata.integration.datasource.dto.DataSourceInternalResponse;
import com.governance.metadata.repository.MetadataCollectionTaskRepository;
import com.governance.metadata.service.MetadataCollectionTaskService;
import com.governance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataCollectionTaskServiceImpl implements MetadataCollectionTaskService {

    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;
    private final DataSourceClient dataSourceClient;
    private final ObjectMapper objectMapper;

    @Override
    public MetadataCollectionTaskResponse createTask(MetadataCollectionTaskRequest request) {
        String normalizedTaskName = request.getTaskName().trim();
        if (metadataCollectionTaskRepository.existsByTaskName(normalizedTaskName)) {
            throw new DuplicateMetadataCollectionTaskException("Task name already exists: " + normalizedTaskName);
        }

        DataSourceInternalResponse dataSource = getDataSourceById(request.getDataSourceId());
        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        MetadataCollectionTask entity = MetadataCollectionTask.builder()
                .taskName(normalizedTaskName)
                .dataSourceId(dataSource.getId())
                .dataSourceName(dataSource.getName())
                .dataSourceType(dataSource.getType())
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

        DataSourceInternalResponse dataSource = getDataSourceById(request.getDataSourceId());
        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        existing.setTaskName(normalizedTaskName);
        existing.setDataSourceId(dataSource.getId());
        existing.setDataSourceName(dataSource.getName());
        existing.setDataSourceType(dataSource.getType());
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

    private DataSourceInternalResponse getDataSourceById(Long dataSourceId) {
        try {
            DataSourceInternalResponse response = dataSourceClient.getById(dataSourceId);
            if (response == null) {
                throw new ResourceNotFoundException("Data source not found: " + dataSourceId);
            }
            return response;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Data source not found: " + dataSourceId);
        }
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
                .dataSourceId(entity.getDataSourceId())
                .dataSourceName(entity.getDataSourceName())
                .dataSourceType(entity.getDataSourceType())
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
