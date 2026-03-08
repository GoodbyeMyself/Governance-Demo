package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证中心登录响应结果。
 * <p>
 * 登录成功后返回访问令牌及当前用户信息，
 * 供前端建立登录态并初始化界面上下文。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应结果")
public class AuthCenterLoginResponse {

    /**
     * 登录成功后签发的访问令牌。
     */
    @Schema(description = "JWT 访问令牌")
    private String token;

    /**
     * 令牌类型，通常为 Bearer。
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    /**
     * 令牌有效期，单位为秒。
     */
    @Schema(description = "令牌有效时长（秒）", example = "86400")
    private Long expiresIn;

    /**
     * 当前登录用户的资料信息。
     */
    @Schema(description = "当前登录用户信息")
    private AuthCenterUserProfileResponse user;
}
