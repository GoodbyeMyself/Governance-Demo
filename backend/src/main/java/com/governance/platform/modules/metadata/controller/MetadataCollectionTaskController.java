package com.governance.platform.modules.metadata.controller;

import com.governance.platform.shared.api.ApiResponse;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskResponse;
import com.governance.platform.modules.metadata.service.MetadataCollectionTaskService;
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
@RequestMapping("/api/metadata-collection-tasks")
@RequiredArgsConstructor
@Tag(name = "元数据采集任务", description = "元数据采集任务的新增、修改、删除与查询接口")
public class MetadataCollectionTaskController {

    private final MetadataCollectionTaskService metadataCollectionTaskService;

    @PostMapping
    @Operation(summary = "新增采集任务", description = "创建一个新的元数据采集任务")
    public ApiResponse<MetadataCollectionTaskResponse> createTask(
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.createTask(request);
        return ApiResponse.success("Metadata collection task created", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情", description = "根据任务ID查询元数据采集任务详情")
    public ApiResponse<MetadataCollectionTaskResponse> getTaskById(
            @Parameter(description = "任务ID", example = "1") @PathVariable Long id
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.getTaskById(id);
        return ApiResponse.success("Success", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新采集任务", description = "根据任务ID更新元数据采集任务")
    public ApiResponse<MetadataCollectionTaskResponse> updateTask(
            @Parameter(description = "任务ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.updateTask(id, request);
        return ApiResponse.success("Metadata collection task updated", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采集任务", description = "根据任务ID删除元数据采集任务")
    public ApiResponse<Void> deleteTask(
            @Parameter(description = "任务ID", example = "1") @PathVariable Long id
    ) {
        metadataCollectionTaskService.deleteTask(id);
        return ApiResponse.success("Metadata collection task deleted", null);
    }

    @GetMapping
    @Operation(summary = "查询任务列表", description = "查询全部元数据采集任务")
    public ApiResponse<List<MetadataCollectionTaskResponse>> getAllTasks() {
        List<MetadataCollectionTaskResponse> responses = metadataCollectionTaskService.getAllTasks();
        return ApiResponse.success("Success", responses);
    }
}



