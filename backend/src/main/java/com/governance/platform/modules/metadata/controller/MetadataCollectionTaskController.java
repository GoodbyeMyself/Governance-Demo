package com.governance.platform.modules.metadata.controller;

import com.governance.platform.shared.api.ApiResponse;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskResponse;
import com.governance.platform.modules.metadata.service.MetadataCollectionTaskService;
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
public class MetadataCollectionTaskController {

    private final MetadataCollectionTaskService metadataCollectionTaskService;

    @PostMapping
    public ApiResponse<MetadataCollectionTaskResponse> createTask(
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.createTask(request);
        return ApiResponse.success("Metadata collection task created", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<MetadataCollectionTaskResponse> getTaskById(@PathVariable Long id) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.getTaskById(id);
        return ApiResponse.success("Success", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<MetadataCollectionTaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody MetadataCollectionTaskRequest request
    ) {
        MetadataCollectionTaskResponse response = metadataCollectionTaskService.updateTask(id, request);
        return ApiResponse.success("Metadata collection task updated", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        metadataCollectionTaskService.deleteTask(id);
        return ApiResponse.success("Metadata collection task deleted", null);
    }

    @GetMapping
    public ApiResponse<List<MetadataCollectionTaskResponse>> getAllTasks() {
        List<MetadataCollectionTaskResponse> responses = metadataCollectionTaskService.getAllTasks();
        return ApiResponse.success("Success", responses);
    }
}



