package com.governance.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "调度方式枚举：MANUAL=手动执行，CRON=定时调度")
public enum MetadataCollectionScheduleType {
    @Schema(description = "手动执行")
    MANUAL,
    @Schema(description = "定时调度")
    CRON
}



