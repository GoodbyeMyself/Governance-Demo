package com.governance.bms.user.controller;

import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.service.UserManagementService;
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
 * 后台用户管理对外接口。
 *
 * <p>该控制器面向后台管理端，负责用户列表、角色管理以及角色/权限枚举输出。</p>
 */
@RestController
@RequestMapping("/api/bms")
@RequiredArgsConstructor
@Tag(name = "基础管理", description = "用户、角色、权限管理接口")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final MessageResolver messageResolver;

    /**
     * 查询用户列表。
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询用户列表", description = "管理员查询系统全部用户")
    public ApiResponse<List<UserProfileResponse>> listUsers() {
        return ApiResponse.success(messageResolver.getMessage("common.success"), userManagementService.listUsers());
    }

    /**
     * 更新指定用户角色。
     *
     * @param id 用户 ID
     * @param request 角色更新请求
     * @return 更新后的用户资料
     */
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改用户角色", description = "管理员根据用户ID修改用户角色")
    public ApiResponse<UserProfileResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("bms.response.userRole.updated"),
                userManagementService.updateUserRole(id, request)
        );
    }

    /**
     * 查询系统支持的角色列表。
     *
     * @return 角色编码列表
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询角色列表", description = "返回系统可分配的角色列表")
    public ApiResponse<List<String>> listRoles() {
        return ApiResponse.success(messageResolver.getMessage("common.success"), userManagementService.listRoles());
    }

    /**
     * 查询系统支持的权限列表。
     *
     * @return 权限编码列表
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询权限列表", description = "返回系统定义的权限标识列表")
    public ApiResponse<List<String>> listPermissions() {
        return ApiResponse.success(
                messageResolver.getMessage("common.success"),
                userManagementService.listPermissions()
        );
    }
}

