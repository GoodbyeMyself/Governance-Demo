package com.governance.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 元数据采集策略枚举。
 */
@Schema(description = "采集策略枚举：FULL=全量采集，INCREMENTAL=增量采集")
public enum MetadataCollectionStrategy {
    /**
     * 全量采集。
     */
    @Schema(description = "全量采集")
    FULL,

    /**
     * 增量采集。
     */
    @Schema(description = "增量采集")
    INCREMENTAL
}
