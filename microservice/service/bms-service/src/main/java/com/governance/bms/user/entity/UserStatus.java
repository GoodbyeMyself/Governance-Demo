package com.governance.bms.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台用户状态枚举。
 */
@Schema(description = "用户状态枚举：ENABLED=启用，DISABLED=禁用")
public enum UserStatus {
    /**
     * 启用状态。
     */
    @Schema(description = "启用")
    ENABLED,

    /**
     * 禁用状态。
     */
    @Schema(description = "禁用")
    DISABLED
}
