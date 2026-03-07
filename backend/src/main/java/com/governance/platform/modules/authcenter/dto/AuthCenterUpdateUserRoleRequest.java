package com.governance.platform.modules.authcenter.dto;

import com.governance.platform.modules.authcenter.entity.AuthCenterUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改用户角色请求参数")
public class AuthCenterUpdateUserRoleRequest {

    @Schema(description = "用户角色", allowableValues = {"USER", "ADMIN"}, example = "ADMIN")
    @NotNull(message = "role is required")
    private AuthCenterUserRole role;
}

