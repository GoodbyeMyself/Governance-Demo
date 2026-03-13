package com.governance.iotcollection.controller;

import com.governance.iotcollection.dto.IotCollectionTaskRequest;
import com.governance.iotcollection.dto.IotCollectionTaskResponse;
import com.governance.iotcollection.service.IotCollectionTaskService;
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
 * IoT 采集任务对外接口。
 */
@RestController
@RequestMapping("/api/iot-collection/tasks")
@RequiredArgsConstructor
@Tag(name = "IoT Collection Task", description = "IoT collection task CRUD APIs")
public class IotCollectionTaskController {

    private final IotCollectionTaskService iotCollectionTaskService;

    @PostMapping
    @Operation(summary = "Create IoT collection task", description = "Create a new IoT collection task")
    public ApiResponse<IotCollectionTaskResponse> createTask(@Valid @RequestBody IotCollectionTaskRequest request) {
        return ApiResponse.success("IoT collection task created", iotCollectionTaskService.createTask(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update IoT collection task", description = "Update an IoT collection task by ID")
    public ApiResponse<IotCollectionTaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody IotCollectionTaskRequest request
    ) {
        return ApiResponse.success("IoT collection task updated", iotCollectionTaskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete IoT collection task", description = "Delete an IoT collection task by ID")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        iotCollectionTaskService.deleteTask(id);
        return ApiResponse.success("IoT collection task deleted", null);
    }

    @GetMapping
    @Operation(summary = "List IoT collection tasks", description = "Query all IoT collection tasks")
    public ApiResponse<List<IotCollectionTaskResponse>> getAllTasks() {
        return ApiResponse.success("Success", iotCollectionTaskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get IoT collection task details", description = "Query an IoT collection task by ID")
    public ApiResponse<IotCollectionTaskResponse> getTaskById(@PathVariable Long id) {
        return ApiResponse.success("Success", iotCollectionTaskService.getTaskById(id));
    }
}
