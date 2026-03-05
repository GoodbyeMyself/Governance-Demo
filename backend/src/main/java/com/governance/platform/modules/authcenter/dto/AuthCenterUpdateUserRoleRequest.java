package com.governance.platform.modules.authcenter.dto;

import com.governance.platform.modules.authcenter.entity.AuthCenterUserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCenterUpdateUserRoleRequest {

    @NotNull(message = "role is required")
    private AuthCenterUserRole role;
}

