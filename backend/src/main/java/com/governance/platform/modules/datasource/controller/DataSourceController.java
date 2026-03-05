package com.governance.platform.modules.datasource.controller;

import com.governance.platform.shared.api.ApiResponse;
import com.governance.platform.modules.datasource.dto.DataSourceRequest;
import com.governance.platform.modules.datasource.dto.DataSourceResponse;
import com.governance.platform.modules.datasource.service.DataSourceService;
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
@RequestMapping("/api/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    public ApiResponse<DataSourceResponse> addDataSource(@Valid @RequestBody DataSourceRequest request) {
        DataSourceResponse response = dataSourceService.createDataSource(request);
        return ApiResponse.success("Data source created", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success("Data source deleted", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<DataSourceResponse> updateDataSource(
            @PathVariable Long id,
            @Valid @RequestBody DataSourceRequest request
    ) {
        DataSourceResponse response = dataSourceService.updateDataSource(id, request);
        return ApiResponse.success("Data source updated", response);
    }

    @GetMapping
    public ApiResponse<List<DataSourceResponse>> getAllDataSources() {
        List<DataSourceResponse> responses = dataSourceService.getAllDataSources();
        return ApiResponse.success("Success", responses);
    }
}



