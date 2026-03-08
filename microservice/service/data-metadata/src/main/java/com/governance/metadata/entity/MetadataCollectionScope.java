package com.governance.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "采集范围枚举：SCHEMA=按库/模式采集，TABLE=按表采集")
public enum MetadataCollectionScope {
    @Schema(description = "按库/模式采集")
    SCHEMA,
    @Schema(description = "按表采集")
    TABLE
}



