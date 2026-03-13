package com.governance.iotcollection.service;

import com.governance.iotcollection.dto.IotCollectionTaskRequest;
import com.governance.iotcollection.dto.IotCollectionTaskResponse;

import java.util.List;

/**
 * IoT 采集任务领域服务接口。
 */
public interface IotCollectionTaskService {

    IotCollectionTaskResponse createTask(IotCollectionTaskRequest request);

    IotCollectionTaskResponse updateTask(Long id, IotCollectionTaskRequest request);

    void deleteTask(Long id);

    List<IotCollectionTaskResponse> getAllTasks();

    IotCollectionTaskResponse getTaskById(Long id);
}
