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
 * 当前用户资料更新请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前用户资料更新请求参数")
public class AuthCenterProfileUpdateRequest {

    /**
     * 用户名。
     */
    @Schema(description = "用户名（4-64 位，仅支持字母、数字和下划线）", example = "new_user_01")
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 4, max = 64, message = "{validation.username.length}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.username.pattern}")
    private String username;

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String email;

    /**
     * 手机号。
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^[0-9+\\-]{6,30}$", message = "{validation.phone.invalid}")
    @Size(max = 30, message = "{validation.phone.max}")
    private String phone;
}
