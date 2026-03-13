package com.governance.iotcollection.service;

import com.governance.iotcollection.dto.IotTelemetryHistoryItemResponse;
import com.governance.iotcollection.dto.IotTelemetryLatestItemResponse;
import com.governance.iotcollection.dto.IotTelemetryOverviewResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 实时数据领域服务接口。
 */
public interface IotTelemetryService {

    List<IotTelemetryLatestItemResponse> getLatestTelemetryByDeviceId(Long deviceId);

    List<IotTelemetryHistoryItemResponse> getTelemetryHistory(
            Long deviceId,
            String metricCode,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    IotTelemetryOverviewResponse getTelemetryOverview();
}
