package com.governance.bms.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色定义响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色定义响应对象")
public class RoleDefinitionResponse {

    /**
     * 角色 ID。
     */
    @Schema(description = "角色 ID", example = "1")
    private Long id;

    /**
     * 角色编码。
     */
    @Schema(description = "角色编码", example = "ADMIN")
    private String roleCode;

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    /**
     * 是否允许编辑。
     */
    @Schema(description = "是否允许编辑", example = "false")
    private boolean editable;

    /**
     * 绑定该角色的用户数量。
     */
    @Schema(description = "用户数量", example = "3")
    private long userCount;
}
