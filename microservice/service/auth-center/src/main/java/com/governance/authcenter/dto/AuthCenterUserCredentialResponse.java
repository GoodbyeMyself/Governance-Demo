package com.governance.authcenter.dto;

import com.governance.authcenter.entity.AuthCenterUserRole;
import com.governance.authcenter.entity.AuthCenterUserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCenterUserCredentialResponse {
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String phone;
    private AuthCenterUserRole role;
    private AuthCenterUserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}