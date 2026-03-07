package com.governance.platform.modules.authcenter.controller;

import com.governance.platform.modules.authcenter.dto.AuthCenterLoginRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterLoginResponse;
import com.governance.platform.modules.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUpdateUserRoleRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.platform.modules.authcenter.service.AuthCenterService;
import com.governance.platform.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth-center")
@RequiredArgsConstructor
@Tag(name = "认证中心", description = "用户注册、登录、当前用户与权限管理接口")
public class AuthCenterController {

    private final AuthCenterService authCenterService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册一个新的平台账号")
    public ApiResponse<AuthCenterUserProfileResponse> register(
            @Valid @RequestBody AuthCenterRegisterRequest request
    ) {
        AuthCenterUserProfileResponse response = authCenterService.register(request);
        return ApiResponse.success("Register successful", response);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT 令牌")
    public ApiResponse<AuthCenterLoginResponse> login(
            @Valid @RequestBody AuthCenterLoginRequest request
    ) {
        AuthCenterLoginResponse response = authCenterService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的个人信息")
    public ApiResponse<AuthCenterUserProfileResponse> me() {
        AuthCenterUserProfileResponse response = authCenterService.me();
        return ApiResponse.success("Success", response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "当前用户退出登录")
    public ApiResponse<Void> logout() {
        authCenterService.logout();
        return ApiResponse.success("Logout successful", null);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询用户列表", description = "管理员查看系统内所有用户")
    public ApiResponse<List<AuthCenterUserProfileResponse>> listUsers() {
        List<AuthCenterUserProfileResponse> users = authCenterService.listUsers();
        return ApiResponse.success("Success", users);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改用户角色", description = "管理员修改指定用户的角色")
    public ApiResponse<AuthCenterUserProfileResponse> updateUserRole(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AuthCenterUpdateUserRoleRequest request
    ) {
        AuthCenterUserProfileResponse response = authCenterService.updateUserRole(id, request);
        return ApiResponse.success("User role updated", response);
    }
}
