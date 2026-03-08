package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应结果")
public class AuthCenterLoginResponse {
    @Schema(description = "JWT 访问令牌")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "令牌有效期（秒）", example = "86400")
    private Long expiresIn;

    @Schema(description = "当前登录用户信息")
    private AuthCenterUserProfileResponse user;
}

