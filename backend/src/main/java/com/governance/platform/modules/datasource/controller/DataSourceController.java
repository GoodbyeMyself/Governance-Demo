package com.governance.platform.modules.datasource.controller;

import com.governance.platform.shared.api.ApiResponse;
import com.governance.platform.modules.datasource.dto.DataSourceRequest;
import com.governance.platform.modules.datasource.dto.DataSourceResponse;
import com.governance.platform.modules.datasource.service.DataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/data-sources")
@RequiredArgsConstructor
@Tag(name = "数据源管理", description = "数据源的新增、修改、删除与查询接口")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    @Operation(summary = "新增数据源", description = "创建一个新的数据源配置")
    public ApiResponse<DataSourceResponse> addDataSource(@Valid @RequestBody DataSourceRequest request) {
        DataSourceResponse response = dataSourceService.createDataSource(request);
        return ApiResponse.success("Data source created", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据源", description = "根据数据源ID删除数据源配置")
    public ApiResponse<Void> deleteDataSource(@Parameter(description = "数据源ID", example = "1") @PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success("Data source deleted", null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据源", description = "根据数据源ID更新数据源配置")
    public ApiResponse<DataSourceResponse> updateDataSource(
            @Parameter(description = "数据源ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody DataSourceRequest request
    ) {
        DataSourceResponse response = dataSourceService.updateDataSource(id, request);
        return ApiResponse.success("Data source updated", response);
    }

    @GetMapping
    @Operation(summary = "查询数据源列表", description = "查询当前系统内的全部数据源")
    public ApiResponse<List<DataSourceResponse>> getAllDataSources() {
        List<DataSourceResponse> responses = dataSourceService.getAllDataSources();
        return ApiResponse.success("Success", responses);
    }
}



