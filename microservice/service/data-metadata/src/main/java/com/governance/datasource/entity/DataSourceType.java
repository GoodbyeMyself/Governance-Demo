package com.governance.datasource.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 元数据服务内部复用的数据源类型枚举。
 * <p>
 * 与数据源服务保持一致，用于在服务间调用和任务持久化时统一类型定义。
 */
@Schema(description = "数据源类型枚举：DATABASE=数据库，FILE_SYSTEM=文件系统")
public enum DataSourceType {
    /**
     * 数据库类型。
     */
    @Schema(description = "数据库")
    DATABASE,

    /**
     * 文件系统类型。
     */
    @Schema(description = "文件系统")
    FILE_SYSTEM
}
