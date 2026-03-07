package com.governance.platform.modules.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "采集策略枚举：FULL=全量采集，INCREMENTAL=增量采集")
public enum MetadataCollectionStrategy {
    @Schema(description = "全量采集")
    FULL,
    @Schema(description = "增量采集")
    INCREMENTAL
}



