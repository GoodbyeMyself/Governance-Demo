package com.governance.datasource.integration.metadata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "${integration.metadata-service.name:data-metadata}",
        path = "${integration.metadata-service.path:/internal/metadata-tasks}"
)
public interface MetadataTaskClient {

    @GetMapping("/count-by-data-source/{id}")
    Long countByDataSourceId(@PathVariable("id") Long dataSourceId);
}
