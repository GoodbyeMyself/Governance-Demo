package com.governance.iotcollection.service;

import com.governance.iotcollection.dto.IotTelemetryHistoryItemResponse;
import com.governance.iotcollection.dto.IotTelemetryLatestItemResponse;
import com.governance.iotcollection.dto.IotTelemetryOverviewResponse;
import com.governance.iotcollection.entity.IotDeviceLatestTelemetry;
import com.governance.iotcollection.entity.IotDeviceTelemetryHistory;
import com.governance.iotcollection.repository.IotDeviceLatestTelemetryRepository;
import com.governance.iotcollection.repository.IotDeviceTelemetryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 实时数据服务实现。
 */
@Service
@RequiredArgsConstructor
public class IotTelemetryServiceImpl implements IotTelemetryService {

    private final IotDeviceLatestTelemetryRepository iotDeviceLatestTelemetryRepository;
    private final IotDeviceTelemetryHistoryRepository iotDeviceTelemetryHistoryRepository;

    @Override
    public List<IotTelemetryLatestItemResponse> getLatestTelemetryByDeviceId(Long deviceId) {
        return iotDeviceLatestTelemetryRepository.findByDeviceIdOrderByUpdatedAtDescIdDesc(deviceId)
                .stream()
                .map(this::toLatestResponse)
                .toList();
    }

    @Override
    public List<IotTelemetryHistoryItemResponse> getTelemetryHistory(
            Long deviceId,
            String metricCode,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        LocalDateTime start = startTime == null ? LocalDate.now().minusDays(7).atStartOfDay() : startTime;
        LocalDateTime end = endTime == null ? LocalDateTime.now() : endTime;

        List<IotDeviceTelemetryHistory> items = (metricCode == null || metricCode.isBlank())
                ? iotDeviceTelemetryHistoryRepository.findByDeviceIdAndCollectedAtBetweenOrderByCollectedAtDescIdDesc(deviceId, start, end)
                : iotDeviceTelemetryHistoryRepository.findByDeviceIdAndMetricCodeAndCollectedAtBetweenOrderByCollectedAtDescIdDesc(deviceId, metricCode.trim(), start, end);

        return items.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public IotTelemetryOverviewResponse getTelemetryOverview() {
        long latestTelemetryCount = iotDeviceLatestTelemetryRepository.count();
        long totalTelemetryRecords = iotDeviceTelemetryHistoryRepository.count();
        long totalTelemetryDevices = iotDeviceLatestTelemetryRepository.findAll()
                .stream()
                .map(IotDeviceLatestTelemetry::getDeviceId)
                .distinct()
                .count();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<IotDeviceLatestTelemetry> latestItems = iotDeviceLatestTelemetryRepository.findByUpdatedAtBetween(start, end);
        Map<LocalDate, Long> countMap = new HashMap<>();
        for (IotDeviceLatestTelemetry item : latestItems) {
            LocalDate day = item.getUpdatedAt().toLocalDate();
            countMap.put(day, countMap.getOrDefault(day, 0L) + 1L);
        }

        List<IotTelemetryOverviewResponse.DailyTrendItem> trendItems = java.util.stream.LongStream.rangeClosed(0, 6)
                .mapToObj(offset -> {
                    LocalDate date = today.minusDays(6 - offset);
                    return IotTelemetryOverviewResponse.DailyTrendItem.builder()
                            .date(date)
                            .count(countMap.getOrDefault(date, 0L))
                            .build();
                })
                .toList();

        List<IotTelemetryOverviewResponse.RecentTelemetryItem> recentItems = iotDeviceLatestTelemetryRepository
                .findTop10ByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(item -> IotTelemetryOverviewResponse.RecentTelemetryItem.builder()
                        .id(item.getId())
                        .deviceId(item.getDeviceId())
                        .deviceName(item.getDeviceName())
                        .metricCode(item.getMetricCode())
                        .metricName(item.getMetricName())
                        .metricValue(item.getMetricValue())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();

        return IotTelemetryOverviewResponse.builder()
                .totalTelemetryDevices(totalTelemetryDevices)
                .totalTelemetryRecords(totalTelemetryRecords)
                .latestTelemetryCount(latestTelemetryCount)
                .telemetryTrend7d(trendItems)
                .recentTelemetryItems(recentItems)
                .build();
    }

    private IotTelemetryLatestItemResponse toLatestResponse(IotDeviceLatestTelemetry entity) {
        return IotTelemetryLatestItemResponse.builder()
                .id(entity.getId())
                .deviceId(entity.getDeviceId())
                .deviceCode(entity.getDeviceCode())
                .deviceName(entity.getDeviceName())
                .productKey(entity.getProductKey())
                .productName(entity.getProductName())
                .metricCode(entity.getMetricCode())
                .metricName(entity.getMetricName())
                .metricValue(entity.getMetricValue())
                .valueType(entity.getValueType())
                .unit(entity.getUnit())
                .quality(entity.getQuality())
                .collectedAt(entity.getCollectedAt())
                .receivedAt(entity.getReceivedAt())
                .sourceType(entity.getSourceType())
                .dataFormat(entity.getDataFormat())
                .payloadJson(entity.getPayloadJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private IotTelemetryHistoryItemResponse toHistoryResponse(IotDeviceTelemetryHistory entity) {
        return IotTelemetryHistoryItemResponse.builder()
                .id(entity.getId())
                .deviceId(entity.getDeviceId())
                .taskId(entity.getTaskId())
                .deviceCode(entity.getDeviceCode())
                .deviceName(entity.getDeviceName())
                .productKey(entity.getProductKey())
                .productName(entity.getProductName())
                .metricCode(entity.getMetricCode())
                .metricName(entity.getMetricName())
                .metricValue(entity.getMetricValue())
                .valueType(entity.getValueType())
                .unit(entity.getUnit())
                .quality(entity.getQuality())
                .collectedAt(entity.getCollectedAt())
                .receivedAt(entity.getReceivedAt())
                .sourceType(entity.getSourceType())
                .dataFormat(entity.getDataFormat())
                .payloadJson(entity.getPayloadJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
