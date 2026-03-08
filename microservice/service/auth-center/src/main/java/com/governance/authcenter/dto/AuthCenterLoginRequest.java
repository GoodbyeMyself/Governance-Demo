package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证中心登录请求参数。
 *
 * <p>登录时要求用户名、密码和图形验证码都必填，
 * 以降低暴力破解和脚本滥用风险。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求参数")
public class AuthCenterLoginRequest {

    /**
     * 登录用户名。
     */
    @Schema(description = "登录用户名", example = "admin")
    @NotBlank(message = "{validation.username.required}")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "Admin@123456")
    @NotBlank(message = "{validation.password.required}")
    private String password;

    /**
     * 图形验证码 ID。
     */
    @Schema(description = "图形验证码 ID")
    @NotBlank(message = "{validation.captchaId.required}")
    private String captchaId;

    /**
     * 图形验证码内容。
     */
    @Schema(description = "图形验证码", example = "7J4K")
    @NotBlank(message = "{validation.captchaCode.required}")
    private String captchaCode;
}
