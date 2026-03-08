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

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }
}

