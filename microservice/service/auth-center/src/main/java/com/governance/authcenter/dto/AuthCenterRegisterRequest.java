package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证中心用户注册请求参数。
 *
 * <p>当前注册流程只保留用户名、密码、邮箱和邮箱验证码，
 * 用户其他资料在注册成功后再补充。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册请求参数")
public class AuthCenterRegisterRequest {

    /**
     * 登录用户名。
     */
    @Schema(description = "用户名（4-64 位，仅支持字母、数字和下划线）", example = "new_user_01")
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 4, max = 64, message = "{validation.username.length}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.username.pattern}")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "密码（8-20 位，必须包含字母和数字）", example = "Password123")
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 20, message = "{validation.password.length}")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.password.pattern}")
    private String password;

    /**
     * 注册邮箱。
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String email;

    /**
     * 邮箱验证码。
     */
    @Schema(description = "邮箱验证码", example = "123456")
    @NotBlank(message = "{validation.emailCode.required}")
    private String emailVerificationCode;
}
