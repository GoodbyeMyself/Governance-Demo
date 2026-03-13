package com.governance.iotcollection.service;

import com.governance.iotcollection.dto.IotCollectionTaskRequest;
import com.governance.iotcollection.dto.IotCollectionTaskResponse;
import com.governance.iotcollection.entity.IotCollectionTask;
import com.governance.iotcollection.exception.DuplicateIotCollectionTaskException;
import com.governance.iotcollection.integration.device.IotDeviceClient;
import com.governance.iotcollection.integration.device.dto.IotDeviceInternalResponse;
import com.governance.iotcollection.repository.IotCollectionTaskRepository;
import com.governance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * IoT 采集任务服务实现。
 */
@Service
@RequiredArgsConstructor
public class IotCollectionTaskServiceImpl implements IotCollectionTaskService {

    private final IotCollectionTaskRepository iotCollectionTaskRepository;
    private final IotDeviceClient iotDeviceClient;

    @Override
    public IotCollectionTaskResponse createTask(IotCollectionTaskRequest request) {
        if (iotCollectionTaskRepository.existsByTaskName(request.getTaskName())) {
            throw new DuplicateIotCollectionTaskException("IoT collection task name already exists: " + request.getTaskName());
        }

        validateTaskRequest(request);

        IotDeviceInternalResponse device = getDeviceById(request.getDeviceId());
        String normalizedTaskName = request.getTaskName().trim();
        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        IotCollectionTask entity = IotCollectionTask.builder()
                .taskName(normalizedTaskName)
                .deviceId(request.getDeviceId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .productKey(StringUtils.hasText(request.getProductKey()) ? request.getProductKey().trim() : device.getProductKey())
                .productName(StringUtils.hasText(request.getProductName()) ? request.getProductName().trim() : device.getProductName())
                .collectionType(request.getCollectionType())
                .scheduleType(request.getScheduleType())
                .cronExpression(cronExpression)
                .pollIntervalSeconds(request.getPollIntervalSeconds())
                .sourceType(trimToNull(request.getSourceType()))
                .dataFormat(trimToNull(request.getDataFormat()))
                .configJson(configJson)
                .enabled(request.getEnabled())
                .description(trimToNull(request.getDescription()))
                .build();

        return toResponse(iotCollectionTaskRepository.save(entity));
    }

    @Override
    public IotCollectionTaskResponse updateTask(Long id, IotCollectionTaskRequest request) {
        IotCollectionTask existing = iotCollectionTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT collection task not found: " + id));

        if (iotCollectionTaskRepository.existsByTaskNameAndIdNot(request.getTaskName(), id)) {
            throw new DuplicateIotCollectionTaskException("IoT collection task name already exists: " + request.getTaskName());
        }

        validateTaskRequest(request);

        IotDeviceInternalResponse device = getDeviceById(request.getDeviceId());
        String normalizedTaskName = request.getTaskName().trim();
        String cronExpression = normalizeCronExpression(request.getScheduleType(), request.getCronExpression());
        String configJson = normalizeConfigJson(request.getConfigJson());

        existing.setTaskName(normalizedTaskName);
        existing.setDeviceId(request.getDeviceId());
        existing.setDeviceCode(device.getDeviceCode());
        existing.setDeviceName(device.getDeviceName());
        existing.setProductKey(StringUtils.hasText(request.getProductKey()) ? request.getProductKey().trim() : device.getProductKey());
        existing.setProductName(StringUtils.hasText(request.getProductName()) ? request.getProductName().trim() : device.getProductName());
        existing.setCollectionType(request.getCollectionType());
        existing.setScheduleType(request.getScheduleType());
        existing.setCronExpression(cronExpression);
        existing.setPollIntervalSeconds(request.getPollIntervalSeconds());
        existing.setSourceType(trimToNull(request.getSourceType()));
        existing.setDataFormat(trimToNull(request.getDataFormat()));
        existing.setConfigJson(configJson);
        existing.setEnabled(request.getEnabled());
        existing.setDescription(trimToNull(request.getDescription()));

        return toResponse(iotCollectionTaskRepository.save(existing));
    }

    @Override
    public void deleteTask(Long id) {
        IotCollectionTask existing = iotCollectionTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT collection task not found: " + id));
        iotCollectionTaskRepository.delete(existing);
    }

    @Override
    public List<IotCollectionTaskResponse> getAllTasks() {
        return iotCollectionTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IotCollectionTaskResponse getTaskById(Long id) {
        IotCollectionTask entity = iotCollectionTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT collection task not found: " + id));
        return toResponse(entity);
    }

    private IotDeviceInternalResponse getDeviceById(Long deviceId) {
        try {
            IotDeviceInternalResponse response = iotDeviceClient.getById(deviceId);
            if (response == null) {
                throw new ResourceNotFoundException("IoT device not found: " + deviceId);
            }
            return response;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("IoT device not found: " + deviceId);
        }
    }

    private String normalizeCronExpression(String scheduleType, String cronExpression) {
        if ("CRON".equalsIgnoreCase(scheduleType)) {
            if (!StringUtils.hasText(cronExpression)) {
                throw new IllegalArgumentException("cronExpression is required when scheduleType is CRON");
            }
            return cronExpression.trim();
        }
        return null;
    }

    private void validateTaskRequest(IotCollectionTaskRequest request) {
        String normalizedScheduleType = request.getScheduleType().trim().toUpperCase();
        Set<String> allowedScheduleTypes = Set.of("CRON", "MANUAL", "POLLING");
        if (!allowedScheduleTypes.contains(normalizedScheduleType)) {
            throw new IllegalArgumentException("scheduleType must be one of CRON, MANUAL, POLLING");
        }

        if (!"CRON".equals(normalizedScheduleType) && request.getPollIntervalSeconds() == null) {
            throw new IllegalArgumentException("pollIntervalSeconds is required when scheduleType is not CRON");
        }

        if (request.getPollIntervalSeconds() != null && request.getPollIntervalSeconds() <= 0) {
            throw new IllegalArgumentException("pollIntervalSeconds must be greater than 0");
        }
    }

    private String normalizeConfigJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return null;
        }
        return configJson.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private IotCollectionTaskResponse toResponse(IotCollectionTask entity) {
        return IotCollectionTaskResponse.builder()
                .id(entity.getId())
                .taskName(entity.getTaskName())
                .deviceId(entity.getDeviceId())
                .deviceCode(entity.getDeviceCode())
                .deviceName(entity.getDeviceName())
                .productKey(entity.getProductKey())
                .productName(entity.getProductName())
                .collectionType(entity.getCollectionType())
                .scheduleType(entity.getScheduleType())
                .cronExpression(entity.getCronExpression())
                .pollIntervalSeconds(entity.getPollIntervalSeconds())
                .sourceType(entity.getSourceType())
                .dataFormat(entity.getDataFormat())
                .configJson(entity.getConfigJson())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
