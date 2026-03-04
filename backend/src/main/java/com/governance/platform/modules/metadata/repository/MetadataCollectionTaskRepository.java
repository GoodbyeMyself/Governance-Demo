package com.governance.platform.modules.metadata.repository;

import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionStrategy;
import com.governance.platform.modules.metadata.entity.MetadataCollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MetadataCollectionTaskRepository extends JpaRepository<MetadataCollectionTask, Long> {
    boolean existsByTaskName(String taskName);

    boolean existsByTaskNameAndIdNot(String taskName, Long id);

    long countByDataSource_Id(Long dataSourceId);

    long countByEnabledTrue();

    long countByScheduleType(MetadataCollectionScheduleType scheduleType);

    long countByStrategy(MetadataCollectionStrategy strategy);

    List<MetadataCollectionTask> findTop5ByOrderByUpdatedAtDescIdDesc();

    List<MetadataCollectionTask> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}



