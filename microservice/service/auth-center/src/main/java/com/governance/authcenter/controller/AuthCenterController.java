package com.governance.authcenter.controller;

import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.authcenter.service.AuthCenterService;
import com.governance.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth-center")
@RequiredArgsConstructor
@Tag(name = "认证中心", description = "用户登录、注册与个人信息接口")
public class AuthCenterController {

    private final AuthCenterService authCenterService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新的平台用户账号")
    public ApiResponse<AuthCenterUserProfileResponse> register(
            @Valid @RequestBody AuthCenterRegisterRequest request
    ) {
        return ApiResponse.success("Register successful", authCenterService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验用户名和密码并签发访问令牌")
    public ApiResponse<AuthCenterLoginResponse> login(
            @Valid @RequestBody AuthCenterLoginRequest request
    ) {
        return ApiResponse.success("Login successful", authCenterService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的资料")
    public ApiResponse<AuthCenterUserProfileResponse> me() {
        return ApiResponse.success("Success", authCenterService.me());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "当前实现为无状态令牌模式，仅返回退出结果")
    public ApiResponse<Void> logout() {
        authCenterService.logout();
        return ApiResponse.success("Logout successful", null);
    }
}
