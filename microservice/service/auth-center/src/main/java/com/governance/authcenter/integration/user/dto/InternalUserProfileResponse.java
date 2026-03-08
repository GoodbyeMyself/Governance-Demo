package com.governance.authcenter.integration.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户中心内部用户资料响应对象。
 * <p>
 * 注册成功或查询当前用户资料时，
 * 认证中心会通过该对象接收用户中心返回的数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserProfileResponse {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户手机号。
     */
    private String phone;

    /**
     * 用户角色编码。
     */
    private String role;

    /**
     * 用户状态编码。
     */
    private String status;

    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
