package com.governance.bms.user.bootstrap;

import com.governance.bms.user.entity.BmsUser;
import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.entity.UserStatus;
import com.governance.bms.user.repository.BmsUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 默认管理员初始化器。
 *
 * <p>服务启动时检查系统内是否已存在管理员账号；
 * 如果不存在，则按照配置创建一个默认管理员，方便首次部署后直接登录。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BmsAdminInitializer implements ApplicationRunner {

    private final BmsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bms.admin.username:admin}")
    private String adminUsername;

    @Value("${bms.admin.password:Admin@123456}")
    private String adminPassword;

    @Value("${bms.admin.nickname:System Admin}")
    private String adminNickname;

    /**
     * 启动后初始化管理员账号。
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        String username = normalizeUsername(adminUsername);
        if (!StringUtils.hasText(username)) {
            username = "admin";
        }

        String password = StringUtils.hasText(adminPassword) ? adminPassword : "Admin@123456";
        String nickname = StringUtils.hasText(adminNickname) ? adminNickname.trim() : "System Admin";

        BmsUser existing = userRepository.findByUsername(username).orElse(null);
        if (existing == null) {
            BmsUser admin = BmsUser.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .status(UserStatus.ENABLED)
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Initialized built-in admin account: {}", username);
            return;
        }

        boolean changed = false;
        if (existing.getRole() != UserRole.ADMIN) {
            existing.setRole(UserRole.ADMIN);
            changed = true;
        }
        if (existing.getStatus() != UserStatus.ENABLED) {
            existing.setStatus(UserStatus.ENABLED);
            changed = true;
        }
        if (!StringUtils.hasText(existing.getNickname())) {
            existing.setNickname(nickname);
            changed = true;
        }

        if (changed) {
            userRepository.save(existing);
            log.info("Updated built-in admin account attributes: {}", username);
        }
    }

    /**
     * 规范化管理员用户名。
     *
     * @param username 原始用户名
     * @return 规范化后的用户名
     */
    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }
}

