package com.governance.authcenter.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 认证中心用户角色枚举。
 * <p>
 * 用于表达用户在平台中的权限层级，
 * 当前区分普通用户与管理员两种角色。
 */
@Schema(description = "用户角色枚举：USER=普通用户，ADMIN=管理员")
public enum AuthCenterUserRole {
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
