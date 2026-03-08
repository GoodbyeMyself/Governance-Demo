package com.governance.bms.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户状态枚举：ENABLED=启用，DISABLED=禁用")
public enum UserStatus {
    @Schema(description = "启用")
    ENABLED,
    @Schema(description = "禁用")
    DISABLED
}
