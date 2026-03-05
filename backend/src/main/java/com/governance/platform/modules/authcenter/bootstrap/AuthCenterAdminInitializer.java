package com.governance.platform.modules.authcenter.bootstrap;

import com.governance.platform.modules.authcenter.entity.AuthCenterUser;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserRole;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserStatus;
import com.governance.platform.modules.authcenter.repository.AuthCenterUserRepository;
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
public class AuthCenterAdminInitializer implements ApplicationRunner {

    private final AuthCenterUserRepository authCenterUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth-center.admin.username:admin}")
    private String adminUsername;

    @Value("${auth-center.admin.password:Admin@123456}")
    private String adminPassword;

    @Value("${auth-center.admin.nickname:System Admin}")
    private String adminNickname;

    @Override
    public void run(ApplicationArguments args) {
        String username = normalizeUsername(adminUsername);
        if (!StringUtils.hasText(username)) {
            username = "admin";
        }

        String password = StringUtils.hasText(adminPassword) ? adminPassword : "Admin@123456";
        String nickname = StringUtils.hasText(adminNickname) ? adminNickname.trim() : "System Admin";

        AuthCenterUser existing = authCenterUserRepository.findByUsername(username).orElse(null);
        if (existing == null) {
            AuthCenterUser admin = AuthCenterUser.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .status(AuthCenterUserStatus.ENABLED)
                    .role(AuthCenterUserRole.ADMIN)
                    .build();
            authCenterUserRepository.save(admin);
            log.info("Initialized built-in admin account: {}", username);
            return;
        }

        boolean changed = false;
        if (existing.getRole() != AuthCenterUserRole.ADMIN) {
            existing.setRole(AuthCenterUserRole.ADMIN);
            changed = true;
        }
        if (existing.getStatus() != AuthCenterUserStatus.ENABLED) {
            existing.setStatus(AuthCenterUserStatus.ENABLED);
            changed = true;
        }
        if (!StringUtils.hasText(existing.getNickname())) {
            existing.setNickname(nickname);
            changed = true;
        }

        if (changed) {
            authCenterUserRepository.save(existing);
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
