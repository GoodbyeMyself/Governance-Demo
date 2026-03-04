package com.governance.platform.modules.metadata.service;

import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.platform.modules.metadata.dto.MetadataCollectionTaskResponse;

import java.util.List;

public interface MetadataCollectionTaskService {
    MetadataCollectionTaskResponse createTask(MetadataCollectionTaskRequest request);

    MetadataCollectionTaskResponse getTaskById(Long id);

    MetadataCollectionTaskResponse updateTask(Long id, MetadataCollectionTaskRequest request);

    void deleteTask(Long id);

    List<MetadataCollectionTaskResponse> getAllTasks();
}



