package com.governance.metadata.repository;

import com.governance.metadata.entity.MetadataCollectionScheduleType;
import com.governance.metadata.entity.MetadataCollectionStrategy;
import com.governance.metadata.entity.MetadataCollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元数据采集任务仓储接口。
 * <p>
 * 封装任务名称校验、启用状态统计、策略统计以及工作台趋势查询等常用数据库访问方法。
 */
public interface MetadataCollectionTaskRepository extends JpaRepository<MetadataCollectionTask, Long> {

    /**
     * 判断任务名称是否已存在。
     *
     * @param taskName 任务名称
     * @return 是否存在
     */
    boolean existsByTaskName(String taskName);

    /**
     * 判断除指定任务外是否存在同名任务。
     *
     * @param taskName 任务名称
     * @param id       当前任务 ID
     * @return 是否存在同名任务
     */
    boolean existsByTaskNameAndIdNot(String taskName, Long id);

    /**
     * 按数据源 ID 统计任务数量。
     *
     * @param dataSourceId 数据源 ID
     * @return 数量结果
     */
    long countByDataSourceId(Long dataSourceId);

    /**
     * 统计已启用任务数量。
     *
     * @return 已启用任务数量
     */
    long countByEnabledTrue();

    /**
     * 按调度方式统计任务数量。
     *
     * @param scheduleType 调度方式
     * @return 数量结果
     */
    long countByScheduleType(MetadataCollectionScheduleType scheduleType);

    /**
     * 按采集策略统计任务数量。
     *
     * @param strategy 采集策略
     * @return 数量结果
     */
    long countByStrategy(MetadataCollectionStrategy strategy);

    /**
     * 查询最近更新的前 5 个任务。
     *
     * @return 最近更新任务列表
     */
    List<MetadataCollectionTask> findTop5ByOrderByUpdatedAtDescIdDesc();

    /**
     * 查询指定时间范围内更新过的任务。
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 区间内更新任务列表
     */
    List<MetadataCollectionTask> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}
