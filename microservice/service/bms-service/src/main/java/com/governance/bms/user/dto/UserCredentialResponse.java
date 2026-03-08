package com.governance.bms.user.dto;

import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 后台用户凭据响应对象。
 * <p>
 * 该对象主要用于内部认证场景，承载密码摘要、角色、状态等完整用户凭据信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialResponse {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 加密后的密码摘要。
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
    private UserRole role;

    /**
     * 用户状态。
     */
    private UserStatus status;

    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
