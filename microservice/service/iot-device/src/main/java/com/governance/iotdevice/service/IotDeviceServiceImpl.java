package com.governance.iotdevice.service;

import com.governance.iotdevice.dto.IotDeviceRequest;
import com.governance.iotdevice.dto.IotDeviceResponse;
import com.governance.iotdevice.entity.IotDeviceInfo;
import com.governance.iotdevice.exception.DuplicateIotDeviceException;
import com.governance.iotdevice.exception.IotDeviceInUseException;
import com.governance.iotdevice.integration.collection.IotCollectionTaskClient;
import com.governance.iotdevice.repository.IotDeviceRepository;
import com.governance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * IoT 设备服务实现。
 */
@Service
@RequiredArgsConstructor
public class IotDeviceServiceImpl implements IotDeviceService {

    private final IotDeviceRepository iotDeviceRepository;
    private final IotCollectionTaskClient iotCollectionTaskClient;

    @Override
    public IotDeviceResponse createDevice(IotDeviceRequest request) {
        String normalizedDeviceCode = request.getDeviceCode().trim();
        if (iotDeviceRepository.existsByDeviceCode(normalizedDeviceCode)) {
            throw new DuplicateIotDeviceException("IoT device code already exists: " + normalizedDeviceCode);
        }

        validateDeviceState(request);

        IotDeviceInfo entity = IotDeviceInfo.builder()
                .deviceCode(normalizedDeviceCode)
                .deviceName(request.getDeviceName().trim())
                .productKey(trimToNull(request.getProductKey()))
                .productName(trimToNull(request.getProductName()))
                .deviceType(request.getDeviceType().trim())
                .protocolType(request.getProtocolType().trim())
                .endpoint(trimToNull(request.getEndpoint()))
                .connectionHost(trimToNull(request.getConnectionHost()))
                .connectionPort(request.getConnectionPort())
                .topicOrPath(trimToNull(request.getTopicOrPath()))
                .username(trimToNull(request.getUsername()))
                .passwordOrSecret(trimToNull(request.getPasswordOrSecret()))
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .onlineStatus(request.getOnlineStatus().trim())
                .status(request.getStatus().trim())
                .description(trimToNull(request.getDescription()))
                .build();

        return toResponse(iotDeviceRepository.save(entity));
    }

    @Override
    public IotDeviceResponse updateDevice(Long id, IotDeviceRequest request) {
        IotDeviceInfo existing = iotDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT device not found: " + id));

        String normalizedDeviceCode = request.getDeviceCode().trim();
        if (iotDeviceRepository.existsByDeviceCodeAndIdNot(normalizedDeviceCode, id)) {
            throw new DuplicateIotDeviceException("IoT device code already exists: " + normalizedDeviceCode);
        }

        validateDeviceState(request);

        existing.setDeviceCode(normalizedDeviceCode);
        existing.setDeviceName(request.getDeviceName().trim());
        existing.setProductKey(trimToNull(request.getProductKey()));
        existing.setProductName(trimToNull(request.getProductName()));
        existing.setDeviceType(request.getDeviceType().trim());
        existing.setProtocolType(request.getProtocolType().trim());
        existing.setEndpoint(trimToNull(request.getEndpoint()));
        existing.setConnectionHost(trimToNull(request.getConnectionHost()));
        existing.setConnectionPort(request.getConnectionPort());
        existing.setTopicOrPath(trimToNull(request.getTopicOrPath()));
        existing.setUsername(trimToNull(request.getUsername()));
        if (StringUtils.hasText(request.getPasswordOrSecret())) {
            existing.setPasswordOrSecret(request.getPasswordOrSecret().trim());
        }
        existing.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        existing.setOnlineStatus(request.getOnlineStatus().trim());
        existing.setStatus(request.getStatus().trim());
        existing.setDescription(trimToNull(request.getDescription()));

        return toResponse(iotDeviceRepository.save(existing));
    }

    @Override
    public void deleteDevice(Long id) {
        IotDeviceInfo existing = iotDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT device not found: " + id));

        long referenceCount = getCollectionTaskReferenceCount(id);
        if (referenceCount > 0) {
            throw new IotDeviceInUseException("IoT device is referenced by collection tasks: " + id);
        }

        iotDeviceRepository.delete(existing);
    }

    @Override
    public List<IotDeviceResponse> getAllDevices() {
        return iotDeviceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IotDeviceResponse getDeviceById(Long id) {
        IotDeviceInfo entity = iotDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IoT device not found: " + id));
        return toResponse(entity);
    }

    private long getCollectionTaskReferenceCount(Long deviceId) {
        try {
            Long count = iotCollectionTaskClient.countByDeviceId(deviceId);
            return count == null ? 0L : count;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify IoT collection task references from iot-collection service");
        }
    }

    private void validateDeviceState(IotDeviceRequest request) {
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        String onlineStatus = request.getOnlineStatus().trim();
        String status = request.getStatus().trim();

        if (!enabled && "ONLINE".equalsIgnoreCase(onlineStatus)) {
            throw new IllegalArgumentException("onlineStatus cannot be ONLINE when enabled is false");
        }
        if (!enabled && "ENABLED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("status cannot be ENABLED when enabled is false");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private IotDeviceResponse toResponse(IotDeviceInfo entity) {
        return IotDeviceResponse.builder()
                .id(entity.getId())
                .deviceCode(entity.getDeviceCode())
                .deviceName(entity.getDeviceName())
                .productKey(entity.getProductKey())
                .productName(entity.getProductName())
                .deviceType(entity.getDeviceType())
                .protocolType(entity.getProtocolType())
                .endpoint(entity.getEndpoint())
                .connectionHost(entity.getConnectionHost())
                .connectionPort(entity.getConnectionPort())
                .topicOrPath(entity.getTopicOrPath())
                .username(entity.getUsername())
                .enabled(entity.getEnabled())
                .onlineStatus(entity.getOnlineStatus())
                .lastOnlineAt(entity.getLastOnlineAt())
                .lastOfflineAt(entity.getLastOfflineAt())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
