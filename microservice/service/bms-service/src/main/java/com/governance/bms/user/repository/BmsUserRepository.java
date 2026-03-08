package com.governance.bms.user.repository;

import com.governance.bms.user.entity.BmsUser;
import com.governance.bms.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 后台用户仓储接口。
 * <p>
 * 封装用户实体在用户管理与认证场景中的常用查询和统计能力。
 */
public interface BmsUserRepository extends JpaRepository<BmsUser, Long> {

    /**
     * 按用户名查询用户。
     *
     * @param username 用户名
     * @return 匹配到的用户信息
     */
    Optional<BmsUser> findByUsername(String username);

    /**
     * 判断用户名是否已存在。
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断除指定用户外是否存在同名用户名。
     *
     * @param username 用户名
     * @param id       当前用户 ID
     * @return 是否存在同名用户
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * 判断邮箱是否已存在。
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 判断除指定用户外是否存在同邮箱用户。
     *
     * @param email 邮箱
     * @param id    当前用户 ID
     * @return 是否存在同邮箱用户
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * 判断手机号是否已存在。
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 判断除指定用户外是否存在同手机号用户。
     *
     * @param phone 手机号
     * @param id    当前用户 ID
     * @return 是否存在同手机号用户
     */
    boolean existsByPhoneAndIdNot(String phone, Long id);

    /**
     * 按角色统计用户数量。
     *
     * @param role 用户角色
     * @return 数量结果
     */
    long countByRole(UserRole role);
}
