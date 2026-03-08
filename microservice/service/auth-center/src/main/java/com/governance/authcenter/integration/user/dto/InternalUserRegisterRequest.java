package com.governance.authcenter.integration.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户中心内部注册请求对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserRegisterRequest {

    /**
     * 用户名。
     */
    private String username;

    /**
     * 原始密码。
     */
    private String password;

    /**
     * 邮箱。
     */
    private String email;
}
