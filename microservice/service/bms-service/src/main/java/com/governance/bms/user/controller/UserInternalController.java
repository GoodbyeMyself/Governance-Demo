package com.governance.bms.user.controller;

import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserPasswordResetRequest;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.dto.UserProfileUpdateRequest;
import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户中心内部接口。
 *
 * <p>该控制器仅供网关内微服务调用，不对前端直接开放。</p>
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Hidden
public class UserInternalController {

    private final UserManagementService userManagementService;

    /**
     * 注册平台用户。
     *
     * @param request 注册请求
     * @return 注册后的用户资料
     */
    @PostMapping("/register")
    public UserProfileResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userManagementService.register(request);
    }

    /**
     * 按用户名查询用户凭据。
     *
     * @param username 用户名
     * @return 用户凭据
     */
    @GetMapping("/by-username")
    public UserCredentialResponse getByUsername(@RequestParam("username") String username) {
        return userManagementService.getByUsername(username);
    }

    /**
     * 更新用户最近登录时间。
     *
     * @param userId 用户 ID
     */
    @PutMapping("/{id}/last-login")
    public void markLastLogin(@PathVariable("id") Long userId) {
        userManagementService.markLastLogin(userId);
    }

    /**
     * 按用户名更新用户资料。
     *
     * @param username 当前用户名
     * @param request  用户资料更新请求
     * @return 更新后的用户资料
     */
    @PutMapping("/by-username/{username}/profile")
    public UserProfileResponse updateUserProfileByUsername(
            @PathVariable("username") String username,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return userManagementService.updateUserProfileByUsername(username, request);
    }

    /**
     * 按用户名重置密码。
     *
     * @param username 用户名
     * @param request  密码重置请求
     */
    @PutMapping("/by-username/{username}/password")
    public void resetPasswordByUsername(
            @PathVariable("username") String username,
            @Valid @RequestBody UserPasswordResetRequest request
    ) {
        userManagementService.resetPasswordByUsername(username, request);
    }

    /**
     * 返回全部用户列表。
     *
     * @return 用户列表
     */
    @GetMapping("/all")
    public List<UserProfileResponse> listUsers() {
        return userManagementService.listUsers();
    }

    /**
     * 更新用户角色。
     *
     * @param userId 用户 ID
     * @param request 角色更新请求
     * @return 更新后的用户资料
     */
    @PutMapping("/{id}/role")
    public UserProfileResponse updateUserRole(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return userManagementService.updateUserRole(userId, request);
    }
}
