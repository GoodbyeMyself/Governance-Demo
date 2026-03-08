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

@RestController
@RequestMapping("/api/data-metadata/tasks")
@RequiredArgsConstructor
@Tag(name = "Metadata Task", description = "Metadata task CRUD APIs")
public class MetadataCollectionTaskController {

    private final MetadataCollectionTaskService metadataCollectionTaskService;

    @PostMapping
    @Operation(summary = "Create task", description = "Create a metadata collection task")
    public ApiResponse<MetadataCollectionTaskResponse> createTask(
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.createTask(request);
        return ApiResponse.success("Metadata collection task created", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task details", description = "Query task details by ID")
    public ApiResponse<MetadataCollectionTaskResponse> getTaskById(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.getTaskById(id);
        return ApiResponse.success("Success", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update a metadata collection task by ID")
    public ApiResponse<MetadataCollectionTaskResponse> updateTask(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.updateTask(id, request);
        return ApiResponse.success("Metadata collection task updated", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete a metadata collection task by ID")
    public ApiResponse<Void> deleteTask(
            @Parameter(description = "Task ID", example = "1") @PathVariable Long id
    ) {
        metadataCollectionTaskService.deleteTask(id);
        return ApiResponse.success("Metadata collection task deleted", null);
    }

    @GetMapping
    @Operation(summary = "List tasks", description = "Query all metadata collection tasks")
    public ApiResponse<List<MetadataCollectionTaskResponse>> getAllTasks() {
        List<MetadataCollectionTaskResponse> responses = metadataCollectionTaskService.getAllTasks();
        return ApiResponse.success("Success", responses);
    }
}
