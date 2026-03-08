package com.governance.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 元数据采集范围枚举。
 */
@Schema(description = "采集范围枚举：SCHEMA=按库/模式采集，TABLE=按表采集")
public enum MetadataCollectionScope {
    /**
     * 按库或模式采集。
     */
    @Schema(description = "按库/模式采集")
    SCHEMA,

    /**
     * 按表采集。
     */
    @Schema(description = "按表采集")
    TABLE
}
