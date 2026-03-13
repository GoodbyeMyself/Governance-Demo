package com.governance.iotcollection.repository;

import com.governance.iotcollection.entity.IotCollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * IoT 采集任务仓储接口。
 */
public interface IotCollectionTaskRepository extends JpaRepository<IotCollectionTask, Long> {

    boolean existsByTaskName(String taskName);

    boolean existsByTaskNameAndIdNot(String taskName, Long id);

    long countByDeviceId(Long deviceId);
}
