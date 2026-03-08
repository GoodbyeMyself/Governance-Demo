package com.governance.bms.user.service;

import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.entity.BmsUser;
import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.entity.UserStatus;
import com.governance.bms.user.exception.DuplicateUserException;
import com.governance.bms.user.exception.UserOperationException;
import com.governance.bms.user.repository.BmsUserRepository;
import com.governance.bms.user.service.UserManagementService;
import com.governance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final BmsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse register(UserRegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        String email = normalizeNullableField(request.getEmail());
        String phone = normalizeNullableField(request.getPhone());

        validateRegisterUnique(username, email, phone);

        BmsUser user = BmsUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(resolveNickname(request.getNickname(), username))
                .email(email)
                .phone(phone)
                .status(UserStatus.ENABLED)
                .role(UserRole.USER)
                .build();

        BmsUser saved = userRepository.save(user);
        return toUserProfile(saved);
    }

    @Override
    public UserCredentialResponse getByUsername(String username) {
        BmsUser user = userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toCredential(user);
    }

    @Override
    public List<UserProfileResponse> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toUserProfile)
                .toList();
    }

    @Override
    public void markLastLogin(Long userId) {
        BmsUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserRole(
            Long userId,
            UserRoleUpdateRequest request
    ) {
        BmsUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserRole newRole = request.getRole();
        UserRole oldRole = user.getRole();
        if (oldRole == newRole) {
            return toUserProfile(user);
        }

        if (oldRole == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new UserOperationException("At least one administrator must remain");
            }
        }

        user.setRole(newRole);
        BmsUser saved = userRepository.save(user);
        return toUserProfile(saved);
    }

    @Override
    public List<String> listRoles() {
        return List.of(UserRole.ADMIN.name(), UserRole.USER.name());
    }

    @Override
    public List<String> listPermissions() {
        return List.of(
                "bms:user:read",
                "bms:user:update-role"
        );
    }

    private void validateRegisterUnique(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("Username already exists: " + username);
        }

        if (email != null && userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email already exists: " + email);
        }

        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new DuplicateUserException("Phone already exists: " + phone);
        }
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("username is required");
        }
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

    private UserProfileResponse toUserProfile(BmsUser user) {
        return UserProfileResponse.builder()
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

    private UserCredentialResponse toCredential(BmsUser user) {
        return UserCredentialResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(user.getPasswordHash())
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

