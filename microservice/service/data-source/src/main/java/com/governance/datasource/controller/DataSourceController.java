package com.governance.datasource.controller;

import com.governance.datasource.dto.DataSourceRequest;
import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.service.DataSourceService;
import com.governance.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data-source")
@RequiredArgsConstructor
@Tag(name = "Data Source", description = "Data source CRUD APIs")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    @Operation(summary = "Create data source", description = "Create a new data source configuration")
    public ApiResponse<DataSourceResponse> addDataSource(@Valid @RequestBody DataSourceRequest request) {
        return ApiResponse.success("Data source created", dataSourceService.createDataSource(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete data source", description = "Delete a data source by ID")
    public ApiResponse<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success("Data source deleted", null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update data source", description = "Update a data source by ID")
    public ApiResponse<DataSourceResponse> updateDataSource(
            @PathVariable Long id,
            @Valid @RequestBody DataSourceRequest request
    ) {
        return ApiResponse.success("Data source updated", dataSourceService.updateDataSource(id, request));
    }

    @GetMapping
    @Operation(summary = "List data sources", description = "Query all data sources")
    public ApiResponse<List<DataSourceResponse>> getAllDataSources() {
        return ApiResponse.success("Success", dataSourceService.getAllDataSources());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get data source details", description = "Query a data source by ID")
    public ApiResponse<DataSourceResponse> getDataSourceById(@PathVariable Long id) {
        return ApiResponse.success("Success", dataSourceService.getDataSourceById(id));
    }
}
