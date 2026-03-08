package com.governance.metadata.integration.datasource;

import com.governance.metadata.integration.datasource.dto.DataSourceInternalResponse;
import com.governance.metadata.integration.datasource.dto.DataSourceStatsOverviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 数据源服务内部调用客户端。
 * <p>
 * 元数据服务通过该 Feign 客户端获取数据源详情以及统计概览，
 * 用于任务创建校验和工作台聚合展示。
 */
@FeignClient(
        name = "${integration.data-source-service.name:data-source}",
        path = "${integration.data-source-service.path:/internal/data-sources}"
)
public interface DataSourceClient {

    /**
     * 按 ID 查询数据源详情。
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    @GetMapping("/{id}")
    DataSourceInternalResponse getById(@PathVariable("id") Long id);

    /**
     * 查询数据源统计概览。
     *
     * @return 数据源概览统计
     */
    @GetMapping("/stats/overview")
    DataSourceStatsOverviewResponse getOverview();
}
