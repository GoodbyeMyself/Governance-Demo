package com.governance.metadata.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 元数据采集任务调度方式枚举。
 */
@Schema(description = "调度方式枚举：MANUAL=手动执行，CRON=定时调度")
public enum MetadataCollectionScheduleType {
    /**
     * 手动执行。
     */
    @Schema(description = "手动执行")
    MANUAL,

    /**
     * 定时调度。
     */
    @Schema(description = "定时调度")
    CRON
}
