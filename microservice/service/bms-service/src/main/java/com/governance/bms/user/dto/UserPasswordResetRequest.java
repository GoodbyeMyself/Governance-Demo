package com.governance.bms.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户密码重置请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户密码重置请求参数")
public class UserPasswordResetRequest {

    /**
     * 新密码。
     */
    @Schema(description = "新密码（8-20 位，必须包含字母和数字）", example = "Password123")
    @NotBlank(message = "{validation.newPassword.required}")
    @Size(min = 8, max = 20, message = "{validation.newPassword.length}")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.newPassword.pattern}")
    private String newPassword;
}
