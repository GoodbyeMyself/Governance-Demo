package com.governance.datasource.service;

import com.governance.datasource.dto.DataSourceRequest;
import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.dto.DataSourceStatsOverviewResponse;

import java.util.List;

/**
 * 数据源领域服务接口。
 *
 * <p>负责数据源的增删改查以及工作台所需概览统计输出。</p>
 */
public interface DataSourceService {

    /**
     * 创建数据源。
     *
     * @param request 数据源请求
     * @return 新建后的数据源
     */
    DataSourceResponse createDataSource(DataSourceRequest request);

    /**
     * 删除数据源。
     *
     * @param id 数据源 ID
     */
    void deleteDataSource(Long id);

    /**
     * 更新数据源。
     *
     * @param id 数据源 ID
     * @param request 更新请求
     * @return 更新后的数据源
     */
    DataSourceResponse updateDataSource(Long id, DataSourceRequest request);

    /**
     * 查询全部数据源。
     *
     * @return 数据源列表
     */
    List<DataSourceResponse> getAllDataSources();

    /**
     * 按 ID 查询数据源。
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    DataSourceResponse getDataSourceById(Long id);

    /**
     * 获取数据源概览统计。
     *
     * @return 概览统计结果
     */
    DataSourceStatsOverviewResponse getOverview();
}



