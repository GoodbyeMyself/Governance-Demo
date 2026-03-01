package com.governance.datasource.controller;

import com.governance.datasource.common.ApiResponse;
import com.governance.datasource.dto.DataSourceRequest;
import com.governance.datasource.dto.DataSourceResponse;
import com.governance.datasource.service.DataSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<DataSourceResponse>> addDataSource(@Valid @RequestBody DataSourceRequest request) {
        DataSourceResponse response = dataSourceService.createDataSource(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Data source created", response));
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
