package com.governance.platform.modules.authcenter.service.impl;

import com.governance.platform.modules.authcenter.dto.AuthCenterLoginRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterLoginResponse;
import com.governance.platform.modules.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUpdateUserRoleRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUserProfileResponse;
import com.governance.platform.modules.authcenter.entity.AuthCenterUser;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserRole;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserStatus;
import com.governance.platform.modules.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.platform.modules.authcenter.exception.AuthCenterDuplicateUserException;
import com.governance.platform.modules.authcenter.exception.AuthCenterOperationException;
import com.governance.platform.modules.authcenter.exception.AuthCenterUserDisabledException;
import com.governance.platform.modules.authcenter.repository.AuthCenterUserRepository;
import com.governance.platform.modules.authcenter.service.AuthCenterService;
import com.governance.platform.shared.security.JwtTokenService;
import com.governance.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthCenterServiceImpl implements AuthCenterService {

    private final AuthCenterUserRepository authCenterUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        String email = normalizeNullableField(request.getEmail());
        String phone = normalizeNullableField(request.getPhone());

        validateRegisterUnique(username, email, phone);

        AuthCenterUser user = AuthCenterUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(resolveNickname(request.getNickname(), username))
                .email(email)
                .phone(phone)
                .status(AuthCenterUserStatus.ENABLED)
                .role(AuthCenterUserRole.USER)
                .build();

        AuthCenterUser saved = authCenterUserRepository.save(user);
        return toUserProfile(saved);
    }

    @Override
    public AuthCenterLoginResponse login(AuthCenterLoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        AuthCenterUser user = authCenterUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthCenterAuthenticationException("Invalid username or password"));

        if (user.getStatus() == AuthCenterUserStatus.DISABLED) {
            throw new AuthCenterUserDisabledException("Account is disabled, please contact administrator");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthCenterAuthenticationException("Invalid username or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        AuthCenterUser saved = authCenterUserRepository.save(user);
        String token = jwtTokenService.generateToken(saved.getUsername());

        return AuthCenterLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpireSeconds())
                .user(toUserProfile(saved))
                .build();
    }

    @Override
    public AuthCenterUserProfileResponse me() {
        String username = getCurrentUsername();
        AuthCenterUser user = authCenterUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthCenterAuthenticationException("Invalid login state, please login again"));
        return toUserProfile(user);
    }

    @Override
    public void logout() {
        // Stateless JWT mode: no server-side token cleanup needed in current version.
    }

    @Override
    public List<AuthCenterUserProfileResponse> listUsers() {
        return authCenterUserRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toUserProfile)
                .toList();
    }

    @Override
    @Transactional
    public AuthCenterUserProfileResponse updateUserRole(
            Long userId,
            AuthCenterUpdateUserRoleRequest request
    ) {
        AuthCenterUser user = authCenterUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        AuthCenterUserRole newRole = request.getRole();
        AuthCenterUserRole oldRole = user.getRole();
        if (oldRole == newRole) {
            return toUserProfile(user);
        }

        if (oldRole == AuthCenterUserRole.ADMIN && newRole != AuthCenterUserRole.ADMIN) {
            long adminCount = authCenterUserRepository.countByRole(AuthCenterUserRole.ADMIN);
            if (adminCount <= 1) {
                throw new AuthCenterOperationException("At least one administrator must remain");
            }
        }

        user.setRole(newRole);
        AuthCenterUser saved = authCenterUserRepository.save(user);
        return toUserProfile(saved);
    }

    private void validateRegisterUnique(String username, String email, String phone) {
        if (authCenterUserRepository.existsByUsername(username)) {
            throw new AuthCenterDuplicateUserException("Username already exists: " + username);
        }

        if (email != null && authCenterUserRepository.existsByEmail(email)) {
            throw new AuthCenterDuplicateUserException("Email already exists: " + email);
        }

        if (phone != null && authCenterUserRepository.existsByPhone(phone)) {
            throw new AuthCenterDuplicateUserException("Phone already exists: " + phone);
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
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableField(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveNickname(String nickname, String username) {
        String normalizedNickname = normalizeNullableField(nickname);
        return normalizedNickname != null ? normalizedNickname : username;
    }

    private AuthCenterUserProfileResponse toUserProfile(AuthCenterUser user) {
        return AuthCenterUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
