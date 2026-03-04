package com.governance.platform.modules.datasource.repository;

import com.governance.platform.modules.datasource.entity.DataSourceInfo;
import com.governance.platform.modules.datasource.entity.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DataSourceRepository extends JpaRepository<DataSourceInfo, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    long countByType(DataSourceType type);

    List<DataSourceInfo> findTop5ByOrderByUpdatedAtDescIdDesc();

    List<DataSourceInfo> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}



