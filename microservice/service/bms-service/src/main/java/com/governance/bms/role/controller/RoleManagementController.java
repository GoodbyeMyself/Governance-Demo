package com.governance.bms.role.controller;

import com.governance.bms.role.dto.RoleDefinitionResponse;
import com.governance.bms.role.dto.RoleDefinitionUpdateRequest;
import com.governance.bms.role.service.RoleManagementService;
import com.governance.shared.api.ApiResponse;
import com.governance.shared.i18n.MessageResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色定义管理接口。
 */
@RestController
@RequestMapping("/api/bms/role-definitions")
@RequiredArgsConstructor
@Tag(name = "角色定义管理", description = "角色定义查询与维护接口")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;
    private final MessageResolver messageResolver;

    /**
     * 查询角色定义列表。
     *
     * @return 角色定义列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询角色定义", description = "管理员查询全部角色定义和用户数量")
    public ApiResponse<List<RoleDefinitionResponse>> listRoleDefinitions() {
        return ApiResponse.success(
                messageResolver.getMessage("common.success"),
                roleManagementService.listRoleDefinitions()
        );
    }

    /**
     * 更新角色定义。
     *
     * @param roleCode 角色编码
     * @param request  更新请求
     * @return 更新后的角色定义
     */
    @PutMapping("/{roleCode}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改角色定义", description = "管理员修改角色名称，ADMIN 角色只读")
    public ApiResponse<RoleDefinitionResponse> updateRoleDefinition(
            @PathVariable("roleCode") String roleCode,
            @Valid @RequestBody RoleDefinitionUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("bms.response.roleDefinition.updated"),
                roleManagementService.updateRoleDefinition(roleCode, request)
        );
    }
}
