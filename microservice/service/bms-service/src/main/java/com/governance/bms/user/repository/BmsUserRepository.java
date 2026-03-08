package com.governance.bms.user.repository;

import com.governance.bms.user.entity.BmsUser;
import com.governance.bms.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BmsUserRepository extends JpaRepository<BmsUser, Long> {
    Optional<BmsUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByRole(UserRole role);
}

