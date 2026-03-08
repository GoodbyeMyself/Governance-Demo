package com.governance.datasource.service;

import com.governance.datasource.dto.DataSourceRequest;
import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.dto.DataSourceStatsOverviewResponse;

import java.util.List;

public interface DataSourceService {
    DataSourceResponse createDataSource(DataSourceRequest request);

    void deleteDataSource(Long id);

    DataSourceResponse updateDataSource(Long id, DataSourceRequest request);

    List<DataSourceResponse> getAllDataSources();

    DataSourceResponse getDataSourceById(Long id);

    DataSourceStatsOverviewResponse getOverview();
}



