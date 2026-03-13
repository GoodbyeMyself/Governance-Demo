package com.governance.iotcollection.repository;

import com.governance.iotcollection.entity.IotDeviceLatestTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 设备最新实时数据仓储接口。
 */
public interface IotDeviceLatestTelemetryRepository extends JpaRepository<IotDeviceLatestTelemetry, Long> {

    List<IotDeviceLatestTelemetry> findByDeviceIdOrderByUpdatedAtDescIdDesc(Long deviceId);

    List<IotDeviceLatestTelemetry> findTop10ByOrderByUpdatedAtDescIdDesc();

    List<IotDeviceLatestTelemetry> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByDeviceId(Long deviceId);
}
