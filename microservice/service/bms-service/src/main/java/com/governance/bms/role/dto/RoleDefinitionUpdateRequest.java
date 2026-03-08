package com.governance.bms.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色定义更新请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色定义更新请求")
public class RoleDefinitionUpdateRequest {

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称", example = "普通用户")
    @NotBlank(message = "{validation.roleName.required}")
    @Size(max = 100, message = "{validation.roleName.max}")
    private String roleName;
}
