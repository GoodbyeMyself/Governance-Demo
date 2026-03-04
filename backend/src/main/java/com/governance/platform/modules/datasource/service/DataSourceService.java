package com.governance.platform.modules.datasource.service;

import com.governance.platform.modules.datasource.dto.DataSourceRequest;
import com.governance.platform.modules.datasource.dto.DataSourceResponse;

import java.util.List;

public interface DataSourceService {
    DataSourceResponse createDataSource(DataSourceRequest request);

    void deleteDataSource(Long id);

    DataSourceResponse updateDataSource(Long id, DataSourceRequest request);

    List<DataSourceResponse> getAllDataSources();
}



