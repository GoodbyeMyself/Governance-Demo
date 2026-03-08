package com.governance.bms.user.dto;

import com.governance.bms.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色更新请求对象。
 * <p>
 * 该对象用于后台管理员调整用户权限时传递目标角色，
 * 保持接口参数语义清晰且便于扩展。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改用户角色请求参数")
public class UserRoleUpdateRequest {

    /**
     * 目标角色。
     */
    @Schema(description = "用户角色", allowableValues = {"USER", "ADMIN"}, example = "ADMIN")
    @NotNull(message = "{validation.role.required}")
    private UserRole role;
}
