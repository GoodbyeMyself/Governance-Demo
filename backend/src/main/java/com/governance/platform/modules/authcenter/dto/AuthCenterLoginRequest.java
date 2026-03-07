package com.governance.platform.modules.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求参数")
public class AuthCenterLoginRequest {

    @Schema(description = "登录用户名", example = "admin")
    @NotBlank(message = "username is required")
    private String username;

    @Schema(description = "登录密码", example = "Admin@123456")
    @NotBlank(message = "password is required")
    private String password;
}
