package com.governance.bms.user.controller;

import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.service.UserManagementService;
import com.governance.shared.api.ApiResponse;
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

@RestController
@RequestMapping("/api/bms")
@RequiredArgsConstructor
@Tag(name = "基础管理", description = "用户、角色、权限管理接口")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询用户列表", description = "管理员查询系统全部用户")
    public ApiResponse<List<UserProfileResponse>> listUsers() {
        return ApiResponse.success("Success", userManagementService.listUsers());
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改用户角色", description = "管理员根据用户ID修改用户角色")
    public ApiResponse<UserProfileResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return ApiResponse.success("User role updated", userManagementService.updateUserRole(id, request));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询角色列表", description = "返回系统可分配的角色列表")
    public ApiResponse<List<String>> listRoles() {
        return ApiResponse.success("Success", userManagementService.listRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询权限列表", description = "返回系统定义的权限标识列表")
    public ApiResponse<List<String>> listPermissions() {
        return ApiResponse.success("Success", userManagementService.listPermissions());
    }
}

