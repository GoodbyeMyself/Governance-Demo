package com.governance.metadata.integration.datasource;

import com.governance.metadata.integration.datasource.dto.DataSourceInternalResponse;
import com.governance.metadata.integration.datasource.dto.DataSourceStatsOverviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "${integration.data-source-service.name:data-source}",
        path = "${integration.data-source-service.path:/internal/data-sources}"
)
public interface DataSourceClient {

    @GetMapping("/{id}")
    DataSourceInternalResponse getById(@PathVariable("id") Long id);

    @GetMapping("/stats/overview")
    DataSourceStatsOverviewResponse getOverview();
}
