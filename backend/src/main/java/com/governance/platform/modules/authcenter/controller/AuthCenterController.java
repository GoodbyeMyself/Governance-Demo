package com.governance.platform.modules.authcenter.controller;

import com.governance.platform.modules.authcenter.dto.AuthCenterLoginRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterLoginResponse;
import com.governance.platform.modules.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUpdateUserRoleRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.platform.modules.authcenter.service.AuthCenterService;
import com.governance.platform.shared.api.ApiResponse;
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
public class AuthCenterController {

    private final AuthCenterService authCenterService;

    @PostMapping("/register")
    public ApiResponse<AuthCenterUserProfileResponse> register(
            @Valid @RequestBody AuthCenterRegisterRequest request
    ) {
        AuthCenterUserProfileResponse response = authCenterService.register(request);
        return ApiResponse.success("Register successful", response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthCenterLoginResponse> login(
            @Valid @RequestBody AuthCenterLoginRequest request
    ) {
        AuthCenterLoginResponse response = authCenterService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @GetMapping("/me")
    public ApiResponse<AuthCenterUserProfileResponse> me() {
        AuthCenterUserProfileResponse response = authCenterService.me();
        return ApiResponse.success("Success", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authCenterService.logout();
        return ApiResponse.success("Logout successful", null);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AuthCenterUserProfileResponse>> listUsers() {
        List<AuthCenterUserProfileResponse> users = authCenterService.listUsers();
        return ApiResponse.success("Success", users);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuthCenterUserProfileResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody AuthCenterUpdateUserRoleRequest request
    ) {
        AuthCenterUserProfileResponse response = authCenterService.updateUserRole(id, request);
        return ApiResponse.success("User role updated", response);
    }
}
