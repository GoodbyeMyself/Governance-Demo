package com.governance.platform.modules.datasource.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "数据源类型枚举：DATABASE=数据库，FILE_SYSTEM=文件系统")
public enum DataSourceType {
    @Schema(description = "数据库")
    DATABASE,
    @Schema(description = "文件系统")
    FILE_SYSTEM
}



