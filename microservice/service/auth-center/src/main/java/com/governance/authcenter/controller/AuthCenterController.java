package com.governance.authcenter.controller;

import com.governance.authcenter.dto.AuthCenterCaptchaResponse;
import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterProfileUpdateRequest;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterResetPasswordRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeResponse;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.authcenter.service.AuthCenterService;
import com.governance.shared.api.ApiResponse;
import com.governance.shared.i18n.MessageResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证中心对外接口。
 *
 * <p>该控制器只保留认证域相关能力，避免再承担后台用户管理职责。</p>
 */
@RestController
@RequestMapping("/api/auth-center")
@RequiredArgsConstructor
@Tag(name = "认证中心", description = "用户登录、注册、验证码、找回密码与个人信息接口")
public class AuthCenterController {

    private final AuthCenterService authCenterService;
    private final MessageResolver messageResolver;

    /**
     * 获取图形验证码。
     *
     * @return 图形验证码
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "生成一次性图形验证码供登录和发送邮箱验证码使用")
    public ApiResponse<AuthCenterCaptchaResponse> getCaptcha() {
        return ApiResponse.success(
                messageResolver.getMessage("common.success"),
                authCenterService.getCaptcha()
        );
    }

    /**
     * 发送邮箱验证码。
     *
     * @param request 发送请求
     * @return 发送结果
     */
    @PostMapping("/email-codes/send")
    @Operation(summary = "发送邮箱验证码", description = "注册和找回密码场景发送邮箱验证码")
    public ApiResponse<AuthCenterSendEmailCodeResponse> sendEmailCode(
            @Valid @RequestBody AuthCenterSendEmailCodeRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("auth.response.emailCode.sent"),
                authCenterService.sendEmailCode(request)
        );
    }

    /**
     * 注册新用户。
     *
     * @param request 注册请求
     * @return 注册后的用户资料
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新的平台用户账号")
    public ApiResponse<AuthCenterUserProfileResponse> register(
            @Valid @RequestBody AuthCenterRegisterRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("auth.response.register.success"),
                authCenterService.register(request)
        );
    }

    /**
     * 用户登录并签发访问令牌。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验用户名、密码和图形验证码并签发访问令牌")
    public ApiResponse<AuthCenterLoginResponse> login(
            @Valid @RequestBody AuthCenterLoginRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("auth.response.login.success"),
                authCenterService.login(request)
        );
    }

    /**
     * 重置用户密码。
     *
     * @param request 重置密码请求
     * @return 空响应
     */
    @PostMapping("/password/reset")
    @Operation(summary = "重置密码", description = "通过邮箱验证码完成找回密码并重置新密码")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody AuthCenterResetPasswordRequest request
    ) {
        authCenterService.resetPassword(request);
        return ApiResponse.success(messageResolver.getMessage("auth.response.password.reset"), null);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户资料
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的资料")
    public ApiResponse<AuthCenterUserProfileResponse> me() {
        return ApiResponse.success(messageResolver.getMessage("common.success"), authCenterService.me());
    }

    /**
     * 更新当前登录用户资料。
     *
     * @param request 资料更新请求
     * @return 更新后的新登录态
     */
    @PutMapping("/me/profile")
    @Operation(summary = "更新当前用户资料", description = "修改用户名、邮箱和手机号，并返回新的登录态")
    public ApiResponse<AuthCenterLoginResponse> updateCurrentUserProfile(
            @Valid @RequestBody AuthCenterProfileUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.getMessage("auth.response.profile.updated"),
                authCenterService.updateCurrentUserProfile(request)
        );
    }

    /**
     * 退出登录。
     *
     * @return 空响应
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "当前实现为无状态令牌模式，仅返回退出结果")
    public ApiResponse<Void> logout() {
        authCenterService.logout();
        return ApiResponse.success(messageResolver.getMessage("auth.response.logout.success"), null);
    }
}
