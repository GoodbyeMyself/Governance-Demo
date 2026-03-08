package com.governance.metadata.controller;

import com.governance.metadata.repository.MetadataCollectionTaskRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 元数据内部接口。
 *
 * <p>用于向其他服务暴露最小必要能力，避免直接共享仓储层实现。</p>
 */
@RestController
@RequestMapping("/internal/metadata-tasks")
@RequiredArgsConstructor
@Hidden
public class InternalMetadataController {

    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    /**
     * 统计指定数据源被采集任务引用的次数。
     *
     * @param dataSourceId 数据源 ID
     * @return 引用次数
     */
    @GetMapping("/count-by-data-source/{id}")
    public Long countByDataSourceId(@PathVariable("id") Long dataSourceId) {
        return metadataCollectionTaskRepository.countByDataSourceId(dataSourceId);
    }
}
