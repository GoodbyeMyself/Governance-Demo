package com.governance.authcenter.integration.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户中心内部凭据响应对象。
 * <p>
 * 认证中心调用用户中心内部接口时，
 * 通过该对象接收认证所需的完整用户资料。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserCredentialResponse {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 密码哈希值。
     */
    private String passwordHash;

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
     * 角色编码字符串。
     */
    private String role;

    /**
     * 状态编码字符串。
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
