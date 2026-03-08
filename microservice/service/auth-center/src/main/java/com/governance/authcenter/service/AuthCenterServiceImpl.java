package com.governance.authcenter.service;

import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.authcenter.entity.AuthCenterUserRole;
import com.governance.authcenter.entity.AuthCenterUserStatus;
import com.governance.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.authcenter.exception.AuthCenterUserDisabledException;
import com.governance.authcenter.integration.user.UserServiceClient;
import com.governance.authcenter.integration.user.dto.InternalUserCredentialResponse;
import com.governance.authcenter.integration.user.dto.InternalUserProfileResponse;
import com.governance.authcenter.integration.user.dto.InternalUserRegisterRequest;
import com.governance.authcenter.service.AuthCenterService;
import com.governance.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthCenterServiceImpl implements AuthCenterService {

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request) {
        request.setUsername(normalizeUsername(request.getUsername()));
        InternalUserRegisterRequest internalRequest = InternalUserRegisterRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        return toUserProfile(userServiceClient.register(internalRequest));
    }

    @Override
    public AuthCenterLoginResponse login(AuthCenterLoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        InternalUserCredentialResponse user = getUserByUsername(username);

        if (parseStatus(user.getStatus()) == AuthCenterUserStatus.DISABLED) {
            throw new AuthCenterUserDisabledException("Account is disabled, please contact administrator");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthCenterAuthenticationException("Invalid username or password");
        }

        userServiceClient.markLastLogin(user.getId());

        String token = jwtTokenService.generateToken(
                user.getUsername(),
                parseRole(user.getRole()).name()
        );
        AuthCenterUserProfileResponse profile = toUserProfile(user);
        profile.setLastLoginAt(LocalDateTime.now());

        return AuthCenterLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpireSeconds())
                .user(profile)
                .build();
    }

    @Override
    public AuthCenterUserProfileResponse me() {
        String username = getCurrentUsername();
        return toUserProfile(getUserByUsername(username));
    }

    @Override
    public void logout() {
        // Stateless JWT mode: no server-side token cleanup needed.
    }

    private InternalUserCredentialResponse getUserByUsername(String username) {
        try {
            return userServiceClient.getByUsername(username);
        } catch (Exception ex) {
            throw new AuthCenterAuthenticationException("Invalid username or password");
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthCenterAuthenticationException("User not logged in");
        }

        String username = authentication.getName();
        if (!StringUtils.hasText(username) || "anonymousUser".equals(username)) {
            throw new AuthCenterAuthenticationException("User not logged in");
        }
        return username;
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new AuthCenterAuthenticationException("Username is required");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private AuthCenterUserRole parseRole(String role) {
        try {
            return AuthCenterUserRole.valueOf(role);
        } catch (Exception ex) {
            throw new AuthCenterAuthenticationException("Invalid role value from user service");
        }
    }

    private AuthCenterUserStatus parseStatus(String status) {
        try {
            return AuthCenterUserStatus.valueOf(status);
        } catch (Exception ex) {
            throw new AuthCenterAuthenticationException("Invalid status value from user service");
        }
    }

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
