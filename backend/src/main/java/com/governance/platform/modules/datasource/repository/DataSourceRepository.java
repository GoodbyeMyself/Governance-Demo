package com.governance.platform.modules.datasource.repository;

import com.governance.platform.modules.datasource.entity.DataSourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository extends JpaRepository<DataSourceInfo, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}



