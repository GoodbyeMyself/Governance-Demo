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

/**
 * 数据源对外接口。
 *
 * <p>提供数据源管理端使用的 CRUD 能力。</p>
 */
@RestController
@RequestMapping("/api/data-source")
@RequiredArgsConstructor
@Tag(name = "Data Source", description = "Data source CRUD APIs")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    /**
     * 创建数据源。
     *
     * @param request 数据源请求
     * @return 新建后的数据源
     */
    @PostMapping
    @Operation(summary = "Create data source", description = "Create a new data source configuration")
    public ApiResponse<DataSourceResponse> addDataSource(@Valid @RequestBody DataSourceRequest request) {
        return ApiResponse.success("Data source created", dataSourceService.createDataSource(request));
    }

    /**
     * 删除数据源。
     *
     * @param id 数据源 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete data source", description = "Delete a data source by ID")
    public ApiResponse<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success("Data source deleted", null);
    }

    /**
     * 更新数据源。
     *
     * @param id 数据源 ID
     * @param request 更新请求
     * @return 更新后的数据源
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update data source", description = "Update a data source by ID")
    public ApiResponse<DataSourceResponse> updateDataSource(
            @PathVariable Long id,
            @Valid @RequestBody DataSourceRequest request
    ) {
        return ApiResponse.success("Data source updated", dataSourceService.updateDataSource(id, request));
    }

    /**
     * 查询全部数据源。
     *
     * @return 数据源列表
     */
    @GetMapping
    @Operation(summary = "List data sources", description = "Query all data sources")
    public ApiResponse<List<DataSourceResponse>> getAllDataSources() {
        return ApiResponse.success("Success", dataSourceService.getAllDataSources());
    }

    /**
     * 按 ID 查询数据源详情。
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get data source details", description = "Query a data source by ID")
    public ApiResponse<DataSourceResponse> getDataSourceById(@PathVariable Long id) {
        return ApiResponse.success("Success", dataSourceService.getDataSourceById(id));
    }
}
