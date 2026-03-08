package com.governance.bms.role.repository;

import com.governance.bms.role.entity.BmsRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 角色定义仓储接口。
 */
public interface BmsRoleRepository extends JpaRepository<BmsRole, Long> {

    /**
     * 按角色编码查询角色定义。
     *
     * @param roleCode 角色编码
     * @return 角色定义
     */
    Optional<BmsRole> findByRoleCode(String roleCode);
}
