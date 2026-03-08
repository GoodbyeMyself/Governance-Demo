package com.governance.datasource.integration.metadata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 元数据任务服务内部调用客户端。
 * <p>
 * 数据源服务通过该客户端查询指定数据源已被多少采集任务引用，
 * 用于删除前的依赖校验。
 */
@FeignClient(
        name = "${integration.metadata-service.name:data-metadata}",
        path = "${integration.metadata-service.path:/internal/metadata-tasks}"
)
public interface MetadataTaskClient {

    /**
     * 按数据源 ID 统计关联任务数量。
     *
     * @param dataSourceId 数据源 ID
     * @return 关联任务数量
     */
    @GetMapping("/count-by-data-source/{id}")
    Long countByDataSourceId(@PathVariable("id") Long dataSourceId);
}
