package com.governance.metadata.repository;

import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionStrategy;
import com.governance.metadata.entity.MetadataCollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MetadataCollectionTaskRepository extends JpaRepository<MetadataCollectionTask, Long> {
    boolean existsByTaskName(String taskName);

    boolean existsByTaskNameAndIdNot(String taskName, Long id);

    long countByDataSourceId(Long dataSourceId);

    long countByEnabledTrue();

    long countByScheduleType(MetadataCollectionScheduleType scheduleType);

    long countByStrategy(MetadataCollectionStrategy strategy);

    List<MetadataCollectionTask> findTop5ByOrderByUpdatedAtDescIdDesc();

    List<MetadataCollectionTask> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}



