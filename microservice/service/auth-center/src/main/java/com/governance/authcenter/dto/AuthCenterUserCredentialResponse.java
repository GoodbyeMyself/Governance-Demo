package com.governance.authcenter.dto;

import com.governance.authcenter.entity.AuthCenterUserRole;
import com.governance.authcenter.entity.AuthCenterUserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 认证中心内部使用的用户凭据响应对象。
 * <p>
 * 该对象承载登录认证所需的完整用户数据，
 * 包括密码摘要、角色、状态等敏感但必要的信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCenterUserCredentialResponse {

    /**
     * 用户主键 ID。
     */
    private Long id;

    /**
     * 登录用户名。
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
     * 用户角色。
     */
    private AuthCenterUserRole role;

    /**
     * 用户状态。
     */
    private AuthCenterUserStatus status;

    /**
     * 最近一次登录时间。
     */
    private LocalDateTime lastLoginAt;

    /**
     * 账号创建时间。
     */
    private LocalDateTime createdAt;
}
