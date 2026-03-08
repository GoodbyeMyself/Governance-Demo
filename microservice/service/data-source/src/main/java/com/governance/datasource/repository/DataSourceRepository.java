package com.governance.datasource.repository;

import com.governance.datasource.entity.DataSourceInfo;
import com.governance.datasource.entity.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源仓储接口。
 * <p>
 * 封装数据源实体的常用存在性校验、分类统计和最近更新查询逻辑。
 */
public interface DataSourceRepository extends JpaRepository<DataSourceInfo, Long> {

    /**
     * 判断指定名称的数据源是否已存在。
     *
     * @param name 数据源名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 判断除指定主键外是否存在同名数据源。
     *
     * @param name 数据源名称
     * @param id   当前数据源 ID
     * @return 是否存在同名记录
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 按数据源类型统计数量。
     *
     * @param type 数据源类型
     * @return 数量结果
     */
    long countByType(DataSourceType type);

    /**
     * 查询最近更新的前 5 个数据源。
     *
     * @return 最近更新列表
     */
    List<DataSourceInfo> findTop5ByOrderByUpdatedAtDescIdDesc();

    /**
     * 查询指定时间范围内更新过的数据源。
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 区间内更新的数据源列表
     */
    List<DataSourceInfo> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}
