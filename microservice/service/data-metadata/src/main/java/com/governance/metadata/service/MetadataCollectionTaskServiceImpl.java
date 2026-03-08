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

/**
 * 元数据采集任务服务实现。
 *
 * <p>负责采集任务的校验、数据源依赖检查、字段规范化以及响应组装。</p>
 */
@Service
@RequiredArgsConstructor
public class MetadataCollectionTaskServiceImpl implements MetadataCollectionTaskService {

    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;
    private final DataSourceClient dataSourceClient;
    private final ObjectMapper objectMapper;

    /**
     * 创建采集任务。
     *
     * @param request 任务请求
     * @return 创建后的任务详情
     */
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

    /**
     * 根据 ID 查询采集任务。
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @Override
    public MetadataCollectionTaskResponse getTaskById(Long id) {
        MetadataCollectionTask task = getTaskEntityById(id);
        return toResponse(task);
    }

    /**
     * 更新采集任务。
     *
     * @param id 任务 ID
     * @param request 更新请求
     * @return 更新后的任务详情
     */
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

    /**
     * 删除采集任务。
     *
     * @param id 任务 ID
     */
    @Override
    public void deleteTask(Long id) {
        MetadataCollectionTask existing = getTaskEntityById(id);
        metadataCollectionTaskRepository.delete(existing);
    }

    /**
     * 查询全部采集任务。
     *
     * @return 任务列表
     */
    @Override
    public List<MetadataCollectionTaskResponse> getAllTasks() {
        return metadataCollectionTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 规范化 Cron 表达式。
     *
     * <p>仅在调度类型为 Cron 时保留表达式，其他调度方式统一返回空值。</p>
     *
     * @param scheduleType 调度类型
     * @param cronExpression 原始 Cron 表达式
     * @return 规范化后的 Cron 表达式
     */
    private String normalizeCronExpression(MetadataCollectionScheduleType scheduleType, String cronExpression) {
        if (scheduleType == MetadataCollectionScheduleType.CRON) {
            if (!StringUtils.hasText(cronExpression)) {
                throw new IllegalArgumentException("cronExpression is required when scheduleType is CRON");
            }
            return cronExpression.trim();
        }
        return null;
    }

    /**
     * 规范化 JSON 配置串。
     *
     * @param configJson 原始 JSON 文本
     * @return 规范化后的 JSON 文本
     */
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

    /**
     * 按数据源 ID 查询数据源信息。
     *
     * @param dataSourceId 数据源 ID
     * @return 数据源内部响应
     */
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

    /**
     * 按 ID 查询任务实体，不存在则抛出异常。
     *
     * @param id 任务 ID
     * @return 任务实体
     */
    private MetadataCollectionTask getTaskEntityById(Long id) {
        return metadataCollectionTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metadata collection task not found: " + id));
    }

    /**
     * 去除字符串首尾空白，空串转为 {@code null}。
     *
     * @param value 原始字符串
     * @return 处理后的值
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 实体转任务响应对象。
     *
     * @param entity 任务实体
     * @return 任务响应
     */
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
