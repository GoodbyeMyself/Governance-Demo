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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册请求参数")
public class AuthCenterRegisterRequest {

    @Schema(description = "用户名（4-64位，仅支持字母/数字/下划线）", example = "new_user_01")
    @NotBlank(message = "username is required")
    @Size(min = 4, max = 64, message = "username length must be between 4 and 64")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username only supports letters, digits and underscore")
    private String username;

    @Schema(description = "密码（8-20位，必须包含字母和数字）", example = "Password123")
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 20, message = "password length must be between 8 and 20")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must include letters and numbers")
    private String password;

    @Schema(description = "昵称", example = "张三")
    @Size(max = 100, message = "nickname length must be at most 100")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be at most 100")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^[0-9+\\-]{6,30}$", message = "phone format is invalid")
    @Size(max = 30, message = "phone length must be at most 30")
    private String phone;
}
