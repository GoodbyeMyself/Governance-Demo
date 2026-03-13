package com.governance.iotcollection.repository;

import com.governance.iotcollection.entity.IotDeviceTelemetryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 设备历史实时数据仓储接口。
 */
public interface IotDeviceTelemetryHistoryRepository extends JpaRepository<IotDeviceTelemetryHistory, Long> {

    List<IotDeviceTelemetryHistory> findByDeviceIdAndCollectedAtBetweenOrderByCollectedAtDescIdDesc(
            Long deviceId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<IotDeviceTelemetryHistory> findByDeviceIdAndMetricCodeAndCollectedAtBetweenOrderByCollectedAtDescIdDesc(
            Long deviceId,
            String metricCode,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByDeviceId(Long deviceId);
}
