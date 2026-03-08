package com.governance.authcenter.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 认证中心用户状态枚举。
 * <p>
 * 用于控制当前账号是否允许参与登录和访问授权。
 */
@Schema(description = "用户状态枚举：ENABLED=启用，DISABLED=禁用")
public enum AuthCenterUserStatus {
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
