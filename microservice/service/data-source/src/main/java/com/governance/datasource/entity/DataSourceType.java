package com.governance.datasource.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 数据源类型枚举。
 * <p>
 * 用于区分当前系统支持的不同数据源接入方式。
 */
@Schema(description = "数据源类型枚举：DATABASE=数据库，FILE_SYSTEM=文件系统")
public enum DataSourceType {
    /**
     * 数据库类型数据源。
     */
    @Schema(description = "数据库")
    DATABASE,

    /**
     * 文件系统类型数据源。
     */
    @Schema(description = "文件系统")
    FILE_SYSTEM
}
