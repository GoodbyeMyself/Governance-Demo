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

@RestController
@RequestMapping("/internal/data-sources")
@RequiredArgsConstructor
@Hidden
public class InternalDataSourceController {

    private final DataSourceService dataSourceService;

    @GetMapping("/{id}")
    public DataSourceResponse getById(@PathVariable Long id) {
        return dataSourceService.getDataSourceById(id);
    }

    @GetMapping("/stats/overview")
    public DataSourceStatsOverviewResponse getOverview() {
        return dataSourceService.getOverview();
    }
}
