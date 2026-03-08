package com.governance.bms.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台用户角色枚举。
 */
@Schema(description = "用户角色枚举：USER=普通用户，ADMIN=管理员")
public enum UserRole {
    /**
     * 普通用户。
     */
    @Schema(description = "普通用户")
    USER,

    /**
     * 管理员。
     */
    @Schema(description = "管理员")
    ADMIN
}
