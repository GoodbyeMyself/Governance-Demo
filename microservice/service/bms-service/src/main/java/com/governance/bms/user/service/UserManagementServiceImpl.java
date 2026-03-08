package com.governance.bms.user.service;

import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserPasswordResetRequest;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.dto.UserProfileUpdateRequest;
import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.entity.BmsUser;
import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.entity.UserStatus;
import com.governance.bms.user.exception.DuplicateUserException;
import com.governance.bms.user.exception.UserOperationException;
import com.governance.bms.user.repository.BmsUserRepository;
import com.governance.shared.exception.ResourceNotFoundException;
import com.governance.shared.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 用户管理服务实现。
 *
 * <p>集中负责用户注册、资料维护、密码更新和角色调整，
 * 让认证中心与后台管理页面都复用同一套业务规则。</p>
 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final BmsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageResolver messageResolver;

    /**
     * 注册平台用户。
     *
     * @param request 用户注册请求
     * @return 注册后的用户资料
     */
    @Override
    @Transactional
    public UserProfileResponse register(UserRegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        String email = normalizeEmail(request.getEmail());

        validateRegisterUnique(username, email, null);

        BmsUser saved = userRepository.save(BmsUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(username)
                .email(email)
                .phone(null)
                .status(UserStatus.ENABLED)
                .role(UserRole.USER)
                .build());
        return toUserProfile(saved);
    }

    /**
     * 按用户名查询用户凭据。
     *
     * @param username 用户名
     * @return 用户凭据
     */
    @Override
    public UserCredentialResponse getByUsername(String username) {
        return toCredential(findByUsernameOrThrow(username));
    }

    /**
     * 查询全部用户。
     *
     * @return 用户列表
     */
    @Override
    public List<UserProfileResponse> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toUserProfile)
                .toList();
    }

    /**
     * 标记用户最近登录时间。
     *
     * @param userId 用户 ID
     */
    @Override
    @Transactional
    public void markLastLogin(Long userId) {
        BmsUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageResolver.getMessage("bms.user.notFound", userId)));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 更新指定用户角色。
     *
     * @param userId 用户 ID
     * @param request 角色更新请求
     * @return 更新后的用户资料
     */
    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long userId, UserRoleUpdateRequest request) {
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
                throw new UserOperationException(messageResolver.getMessage("bms.user.admin.mustRemain"));
            }
        }

        user.setRole(newRole);
        return toUserProfile(userRepository.save(user));
    }

    /**
     * 按用户名更新用户资料。
     *
     * @param currentUsername 当前用户名
     * @param request         用户资料更新请求
     * @return 更新后的用户资料
     */
    @Override
    @Transactional
    public UserProfileResponse updateUserProfileByUsername(
            String currentUsername,
            UserProfileUpdateRequest request
    ) {
        BmsUser user = findByUsernameOrThrow(currentUsername);

        String newUsername = normalizeUsername(request.getUsername());
        String newEmail = normalizeEmail(request.getEmail());
        String newPhone = normalizeNullableField(request.getPhone());

        validateUpdateUnique(user.getId(), newUsername, newEmail, newPhone);

        String oldUsername = user.getUsername();
        boolean shouldSyncNickname = !StringUtils.hasText(user.getNickname())
                || oldUsername.equalsIgnoreCase(user.getNickname().trim());

        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setPhone(newPhone);
        if (shouldSyncNickname) {
            user.setNickname(newUsername);
        }

        return toUserProfile(userRepository.save(user));
    }

    /**
     * 按用户名重置密码。
     *
     * @param username 用户名
     * @param request  密码重置请求
     */
    @Override
    @Transactional
    public void resetPasswordByUsername(String username, UserPasswordResetRequest request) {
        BmsUser user = findByUsernameOrThrow(username);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 返回系统支持的角色列表。
     *
     * @return 角色编码列表
     */
    @Override
    public List<String> listRoles() {
        return List.of(UserRole.ADMIN.name(), UserRole.USER.name());
    }

    /**
     * 返回系统支持的权限列表。
     *
     * @return 权限编码列表
     */
    @Override
    public List<String> listPermissions() {
        return List.of(
                "bms:user:read",
                "bms:user:update-role",
                "bms:role:read",
                "bms:role:update"
        );
    }

    /**
     * 根据用户名查询用户实体，不存在时抛出统一异常。
     *
     * @param username 用户名
     * @return 用户实体
     */
    private BmsUser findByUsernameOrThrow(String username) {
        String normalizedUsername = normalizeUsername(username);
        return userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageResolver.getMessage("bms.user.notFound", normalizedUsername)
                ));
    }

    /**
     * 校验注册场景下的唯一性约束。
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     */
    private void validateRegisterUnique(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.username.exists", username));
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.email.exists", email));
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.phone.exists", phone));
        }
    }

    /**
     * 校验资料更新场景下的唯一性约束。
     *
     * @param userId   当前用户 ID
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     */
    private void validateUpdateUnique(Long userId, String username, String email, String phone) {
        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.username.exists", username));
        }
        if (email != null && userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.email.exists", email));
        }
        if (phone != null && userRepository.existsByPhoneAndIdNot(phone, userId)) {
            throw new DuplicateUserException(messageResolver.getMessage("bms.user.phone.exists", phone));
        }
    }

    /**
     * 规范化用户名。
     *
     * @param username 原始用户名
     * @return 规范化后的用户名
     */
    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException(messageResolver.getMessage("bms.user.username.required"));
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
        String normalizedEmail = normalizeNullableField(email);
        return normalizedEmail == null ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化可空字段。
     *
     * @param value 原始值
     * @return 去空白后的值；空字符串返回 {@code null}
     */
    private String normalizeNullableField(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 把用户实体转换为资料响应。
     *
     * @param user 用户实体
     * @return 用户资料响应
     */
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

    /**
     * 把用户实体转换为认证所需的凭据响应。
     *
     * @param user 用户实体
     * @return 用户凭据响应
     */
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
