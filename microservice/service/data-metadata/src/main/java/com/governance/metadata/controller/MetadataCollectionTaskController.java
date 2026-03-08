package com.governance.metadata.controller;

import com.governance.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.metadata.dto.MetadataCollectionTaskResponse;
import com.governance.metadata.service.MetadataCollectionTaskService;
import com.governance.shared.api.ApiResponse;
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

/**
 * 元数据采集任务对外接口。
 *
 * <p>面向后台管理端，负责采集任务的增删改查。</p>
 */
@RestController
@RequestMapping("/api/data-metadata/tasks")
@RequiredArgsConstructor
@Tag(name = "Metadata Task", description = "Metadata task CRUD APIs")
public class MetadataCollectionTaskController {

    private final MetadataCollectionTaskService metadataCollectionTaskService;

    /**
     * 创建采集任务。
     *
     * @param request 任务请求
     * @return 创建后的任务
     */
    @PostMapping
    @Operation(summary = "Create task", description = "Create a metadata collection task")
    public ApiResponse<MetadataCollectionTaskResponse> createTask(
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.createTask(request);
        return ApiResponse.success("Metadata collection task created", response);
    }

    /**
     * 根据 ID 查询采集任务详情。
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task details", description = "Query task details by ID")
    public ApiResponse<MetadataCollectionTaskResponse> getTaskById(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.getTaskById(id);
        return ApiResponse.success("Success", response);
    }

    /**
     * 更新采集任务。
     *
     * @param id 任务 ID
     * @param request 更新请求
     * @return 更新后的任务详情
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update a metadata collection task by ID")
    public ApiResponse<MetadataCollectionTaskResponse> updateTask(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.updateTask(id, request);
        return ApiResponse.success("Metadata collection task updated", response);
    }

    /**
     * 删除采集任务。
     *
     * @param id 任务 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete a metadata collection task by ID")
    public ApiResponse<Void> deleteTask(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        metadataCollectionTaskService.deleteTask(id);
        return ApiResponse.success("Metadata collection task deleted", null);
    }

    /**
     * 查询全部采集任务。
     *
     * @return 任务列表
     */
    @GetMapping
    @Operation(summary = "List tasks", description = "Query all metadata collection tasks")
    public ApiResponse<List<MetadataCollectionTaskResponse>> getAllTasks() {
        List<MetadataCollectionTaskResponse> responses = metadataCollectionTaskService.getAllTasks();
        return ApiResponse.success("Success", responses);
    }
}
