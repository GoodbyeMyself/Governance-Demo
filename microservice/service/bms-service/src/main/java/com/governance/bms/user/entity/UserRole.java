package com.governance.bms.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户角色枚举：USER=普通用户，ADMIN=管理员")
public enum UserRole {
    @Schema(description = "普通用户")
    USER,
    @Schema(description = "管理员")
    ADMIN
}
