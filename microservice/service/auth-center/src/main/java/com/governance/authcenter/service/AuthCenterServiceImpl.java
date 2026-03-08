package com.governance.authcenter.service;

import com.governance.authcenter.captcha.CaptchaService;
import com.governance.authcenter.dto.AuthCenterCaptchaResponse;
import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterProfileUpdateRequest;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterResetPasswordRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeResponse;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.authcenter.entity.AuthCenterUserRole;
import com.governance.authcenter.entity.AuthCenterUserStatus;
import com.governance.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.authcenter.exception.AuthCenterUserDisabledException;
import com.governance.authcenter.integration.user.UserServiceClient;
import com.governance.authcenter.integration.user.dto.InternalUserCredentialResponse;
import com.governance.authcenter.integration.user.dto.InternalUserPasswordResetRequest;
import com.governance.authcenter.integration.user.dto.InternalUserProfileResponse;
import com.governance.authcenter.integration.user.dto.InternalUserProfileUpdateRequest;
import com.governance.authcenter.integration.user.dto.InternalUserRegisterRequest;
import com.governance.authcenter.security.JwtTokenService;
import com.governance.authcenter.verification.EmailVerificationCodeService;
import com.governance.authcenter.verification.EmailVerificationScene;
import com.governance.shared.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * 认证中心服务实现。
 *
 * <p>该实现通过调用基础管理服务获取用户主数据，
 * 并在本地完成验证码校验、密码校验、邮件验证码校验、JWT 签发和当前用户解析。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthCenterServiceImpl implements AuthCenterService {

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CaptchaService captchaService;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final MessageResolver messageResolver;

    /**
     * 生成图形验证码。
     *
     * @return 图形验证码响应
     */
    @Override
    public AuthCenterCaptchaResponse getCaptcha() {
        return captchaService.generateCaptcha();
    }

    /**
     * 发送邮箱验证码。
     *
     * @param request 发送请求
     * @return 发送结果
     */
    @Override
    public AuthCenterSendEmailCodeResponse sendEmailCode(AuthCenterSendEmailCodeRequest request) {
        captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode());

        String email = normalizeEmail(request.getEmail());
        EmailVerificationScene scene = request.getScene();
        if (scene == EmailVerificationScene.RESET_PASSWORD) {
            String username = normalizeUsername(request.getUsername());
            InternalUserCredentialResponse user = loadUserByUsername(
                    username,
                    () -> new AuthCenterOperationException(messageResolver.getMessage("auth.usernameEmail.invalid"))
            );
            ensureEmailMatches(user, email);
        }

        return emailVerificationCodeService.sendCode(scene, email);
    }

    /**
     * 注册平台用户。
     *
     * @param request 注册请求
     * @return 注册后的用户资料
     */
    @Override
    public AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        String email = normalizeEmail(request.getEmail());

        emailVerificationCodeService.validateCode(
                EmailVerificationScene.REGISTER,
                email,
                request.getEmailVerificationCode()
        );

        InternalUserRegisterRequest internalRequest = InternalUserRegisterRequest.builder()
                .username(username)
                .password(request.getPassword())
                .email(email)
                .build();
        return toUserProfile(userServiceClient.register(internalRequest));
    }

    /**
     * 执行登录流程。
     *
     * <p>流程包括：校验图形验证码、规范化用户名、查询用户、校验状态、校验密码、
     * 更新最后登录时间并签发 JWT。</p>
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @Override
    public AuthCenterLoginResponse login(AuthCenterLoginRequest request) {
        captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode());

        String username = normalizeUsername(request.getUsername());
        InternalUserCredentialResponse user = loadUserByUsername(
                username,
                () -> new AuthCenterAuthenticationException(messageResolver.getMessage("auth.credentials.invalid"))
        );

        if (parseStatus(user.getStatus()) == AuthCenterUserStatus.DISABLED) {
            throw new AuthCenterUserDisabledException(messageResolver.getMessage("auth.account.disabled"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.credentials.invalid"));
        }

        userServiceClient.markLastLogin(user.getId());
        AuthCenterUserProfileResponse profile = toUserProfile(user);
        profile.setLastLoginAt(LocalDateTime.now());
        return buildLoginResponse(profile);
    }

    /**
     * 重置用户密码。
     *
     * @param request 重置密码请求
     */
    @Override
    public void resetPassword(AuthCenterResetPasswordRequest request) {
        String username = normalizeUsername(request.getUsername());
        String email = normalizeEmail(request.getEmail());

        InternalUserCredentialResponse user = loadUserByUsername(
                username,
                () -> new AuthCenterOperationException(messageResolver.getMessage("auth.usernameEmail.invalid"))
        );
        ensureEmailMatches(user, email);

        emailVerificationCodeService.validateCode(
                EmailVerificationScene.RESET_PASSWORD,
                email,
                request.getEmailVerificationCode()
        );

        userServiceClient.resetPasswordByUsername(
                username,
                InternalUserPasswordResetRequest.builder()
                        .newPassword(request.getNewPassword())
                        .build()
        );
    }

    /**
     * 获取当前登录用户资料。
     *
     * @return 当前用户资料
     */
    @Override
    public AuthCenterUserProfileResponse me() {
        String username = getCurrentUsername();
        InternalUserCredentialResponse user = loadUserByUsername(
                username,
                () -> new AuthCenterAuthenticationException(messageResolver.getMessage("auth.user.notFoundOrExpired"))
        );
        return toUserProfile(user);
    }

    /**
     * 更新当前登录用户资料。
     *
     * @param request 资料更新请求
     * @return 新的登录态信息
     */
    @Override
    public AuthCenterLoginResponse updateCurrentUserProfile(AuthCenterProfileUpdateRequest request) {
        String currentUsername = getCurrentUsername();
        InternalUserProfileResponse updatedUser = userServiceClient.updateUserProfileByUsername(
                currentUsername,
                InternalUserProfileUpdateRequest.builder()
                        .username(normalizeUsername(request.getUsername()))
                        .email(normalizeEmail(request.getEmail()))
                        .phone(normalizeNullableField(request.getPhone()))
                        .build()
        );
        return buildLoginResponse(toUserProfile(updatedUser));
    }

    /**
     * 执行退出登录。
     *
     * <p>当前采用无状态 JWT，不需要服务端清理会话或 token。</p>
     */
    @Override
    public void logout() {
    }

    /**
     * 根据用户名获取用户凭据。
     *
     * @param username          用户名
     * @param exceptionSupplier 异常构造器
     * @return 用户凭据
     */
    private InternalUserCredentialResponse loadUserByUsername(
            String username,
            Supplier<? extends RuntimeException> exceptionSupplier
    ) {
        try {
            return userServiceClient.getByUsername(username);
        } catch (Exception ex) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 校验用户名与邮箱是否匹配。
     *
     * @param user  用户凭据
     * @param email 邮箱
     */
    private void ensureEmailMatches(InternalUserCredentialResponse user, String email) {
        String currentEmail = normalizeOptionalEmail(user.getEmail());
        if (!StringUtils.hasText(currentEmail) || !currentEmail.equalsIgnoreCase(email)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.usernameEmail.invalid"));
        }
    }

    /**
     * 从安全上下文中提取当前登录用户名。
     *
     * @return 当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.user.notLoggedIn"));
        }

        String username = authentication.getName();
        if (!StringUtils.hasText(username) || "anonymousUser".equals(username)) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.user.notLoggedIn"));
        }
        return username;
    }

    /**
     * 构建登录态响应。
     *
     * @param profile 当前用户资料
     * @return 登录态响应
     */
    private AuthCenterLoginResponse buildLoginResponse(AuthCenterUserProfileResponse profile) {
        String token = jwtTokenService.generateToken(profile.getUsername(), profile.getRole().name());
        return AuthCenterLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpireSeconds())
                .user(profile)
                .build();
    }

    /**
     * 规范化用户名。
     *
     * @param username 原始用户名
     * @return 规范化后的用户名
     */
    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.username.required"));
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化邮箱。
     *
     * @param email 原始邮箱
     * @return 规范化后的邮箱
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.email.required"));
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化可空邮箱。
     *
     * @param email 原始邮箱
     * @return 规范化后的邮箱；为空时返回 {@code null}
     */
    private String normalizeOptionalEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化可空字段。
     *
     * @param value 原始值
     * @return 去空白后的值；若为空则返回 {@code null}
     */
    private String normalizeNullableField(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 把角色字符串转换为认证中心枚举。
     *
     * @param role 角色编码
     * @return 角色枚举
     */
    private AuthCenterUserRole parseRole(String role) {
        try {
            return AuthCenterUserRole.valueOf(role);
        } catch (Exception ex) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.userService.role.invalid"));
        }
    }

    /**
     * 把状态字符串转换为认证中心枚举。
     *
     * @param status 状态编码
     * @return 状态枚举
     */
    private AuthCenterUserStatus parseStatus(String status) {
        try {
            return AuthCenterUserStatus.valueOf(status);
        } catch (Exception ex) {
            throw new AuthCenterAuthenticationException(messageResolver.getMessage("auth.userService.status.invalid"));
        }
    }

    /**
     * 将内部凭据对象转换为对外用户资料。
     *
     * @param user 内部用户凭据
     * @return 对外用户资料
     */
    private AuthCenterUserProfileResponse toUserProfile(InternalUserCredentialResponse user) {
        return AuthCenterUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(parseRole(user.getRole()))
                .status(parseStatus(user.getStatus()))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 将内部用户资料对象转换为认证中心返回模型。
     *
     * @param user 内部用户资料
     * @return 对外用户资料
     */
    private AuthCenterUserProfileResponse toUserProfile(InternalUserProfileResponse user) {
        return AuthCenterUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(parseRole(user.getRole()))
                .status(parseStatus(user.getStatus()))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
