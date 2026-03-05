package com.governance.platform.modules.authcenter.repository;

import com.governance.platform.modules.authcenter.entity.AuthCenterUser;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthCenterUserRepository extends JpaRepository<AuthCenterUser, Long> {
    Optional<AuthCenterUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByRole(AuthCenterUserRole role);
}
