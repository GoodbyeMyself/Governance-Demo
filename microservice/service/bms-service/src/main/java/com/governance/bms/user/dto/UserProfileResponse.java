package com.governance.bms.user.dto;

import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 后台管理系统用户资料响应对象。
 * <p>
 * 用于用户管理页面、当前登录用户信息等接口的统一返回模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserProfileResponse {

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "1")
    private Long id;

    /**
     * 用户名。
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 用户昵称。
     */
    @Schema(description = "昵称", example = "系统管理员")
    private String nickname;

    /**
     * 用户邮箱。
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 用户手机号。
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 角色。
     */
    @Schema(description = "角色", allowableValues = {"USER", "ADMIN"}, example = "ADMIN")
    private UserRole role;

    /**
     * 账号状态。
     */
    @Schema(description = "账号状态", allowableValues = {"ENABLED", "DISABLED"}, example = "ENABLED")
    private UserStatus status;

    /**
     * 最近登录时间。
     */
    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
