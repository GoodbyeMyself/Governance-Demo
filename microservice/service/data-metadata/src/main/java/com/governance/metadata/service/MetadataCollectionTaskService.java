package com.governance.metadata.service;

import com.governance.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.metadata.dto.MetadataCollectionTaskResponse;

import java.util.List;

public interface MetadataCollectionTaskService {
    MetadataCollectionTaskResponse createTask(MetadataCollectionTaskRequest request);

    MetadataCollectionTaskResponse getTaskById(Long id);

    MetadataCollectionTaskResponse updateTask(Long id, MetadataCollectionTaskRequest request);

    void deleteTask(Long id);

    List<MetadataCollectionTaskResponse> getAllTasks();
}



