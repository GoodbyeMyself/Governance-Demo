package com.governance.platform.modules.authcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCenterLoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private AuthCenterUserProfileResponse user;
}

