package com.governance.platform.modules.authcenter.dto;

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
public class AuthCenterRegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 4, max = 64, message = "username length must be between 4 and 64")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username only supports letters, digits and underscore")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 20, message = "password length must be between 8 and 20")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must include letters and numbers")
    private String password;

    @Size(max = 100, message = "nickname length must be at most 100")
    private String nickname;

    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be at most 100")
    private String email;

    @Pattern(regexp = "^[0-9+\\-]{6,30}$", message = "phone format is invalid")
    @Size(max = 30, message = "phone length must be at most 30")
    private String phone;
}
