package com.governance.platform.modules.metadata.repository;

import com.governance.platform.modules.metadata.entity.MetadataCollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataCollectionTaskRepository extends JpaRepository<MetadataCollectionTask, Long> {
    boolean existsByTaskName(String taskName);

    boolean existsByTaskNameAndIdNot(String taskName, Long id);

    long countByDataSource_Id(Long dataSourceId);
}



