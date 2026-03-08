package com.governance.metadata.service;

import com.governance.metadata.dto.MetadataCollectionTaskRequest;
import com.governance.metadata.dto.MetadataCollectionTaskResponse;

import java.util.List;

/**
 * 元数据采集任务领域服务接口。
 *
 * <p>负责采集任务的增删改查与规则化处理。</p>
 */
public interface MetadataCollectionTaskService {

    /**
     * 创建采集任务。
     *
     * @param request 任务请求
     * @return 创建后的任务详情
     */
    MetadataCollectionTaskResponse createTask(MetadataCollectionTaskRequest request);

    /**
     * 根据 ID 查询采集任务。
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    MetadataCollectionTaskResponse getTaskById(Long id);

    /**
     * 更新采集任务。
     *
     * @param id 任务 ID
     * @param request 更新请求
     * @return 更新后的任务详情
     */
    MetadataCollectionTaskResponse updateTask(Long id, MetadataCollectionTaskRequest request);

    /**
     * 删除采集任务。
     *
     * @param id 任务 ID
     */
    void deleteTask(Long id);

    /**
     * 查询全部采集任务。
     *
     * @return 任务列表
     */
    List<MetadataCollectionTaskResponse> getAllTasks();
}



