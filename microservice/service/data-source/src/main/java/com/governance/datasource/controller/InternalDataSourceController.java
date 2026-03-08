package com.governance.datasource.controller;

import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.dto.DataSourceStatsOverviewResponse;
import com.governance.datasource.service.DataSourceService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源内部接口。
 *
 * <p>仅供元数据服务和工作台聚合逻辑调用。</p>
 */
@RestController
@RequestMapping("/internal/data-sources")
@RequiredArgsConstructor
@Hidden
public class InternalDataSourceController {

    private final DataSourceService dataSourceService;

    /**
     * 按 ID 查询数据源详情。
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    @GetMapping("/{id}")
    public DataSourceResponse getById(@PathVariable Long id) {
        return dataSourceService.getDataSourceById(id);
    }

    /**
     * 返回数据源概览统计。
     *
     * @return 数据源概览
     */
    @GetMapping("/stats/overview")
    public DataSourceStatsOverviewResponse getOverview() {
        return dataSourceService.getOverview();
    }
}
