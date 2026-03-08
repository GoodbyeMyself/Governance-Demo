package com.governance.authcenter.dto;

import com.governance.authcenter.verification.EmailVerificationScene;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送邮箱验证码请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送邮箱验证码请求参数")
public class AuthCenterSendEmailCodeRequest {

    /**
     * 发送场景。
     */
    @Schema(description = "验证码场景", example = "REGISTER")
    @NotNull(message = "{validation.scene.required}")
    private EmailVerificationScene scene;

    /**
     * 邮箱地址。
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String email;

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

    /**
     * 用户名。
     *
     * <p>注册场景可不传；找回密码场景必须传入，
     * 用于校验用户名和邮箱是否匹配。</p>
     */
    @Schema(description = "用户名（找回密码场景必填）", example = "new_user_01")
    @Size(min = 4, max = 64, message = "{validation.username.length}")
    @Pattern(
            regexp = "^$|^[a-zA-Z0-9_]+$",
            message = "{validation.username.pattern}"
    )
    private String username;
}
