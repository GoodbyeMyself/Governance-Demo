package com.governance.bms.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 后台管理用户实体。
 * <p>
 * 该实体映射系统统一用户表，负责承载登录账号、联系信息、
 * 角色状态以及审计时间等核心字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "sys_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sys_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_sys_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_sys_users_phone", columnNames = "phone")
        }
)
public class BmsUser {

    /**
     * 用户主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名，系统内唯一。
     */
    @Column(nullable = false, length = 64)
    private String username;

    /**
     * 加密后的密码摘要。
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * 用户昵称。
     */
    @Column(length = 100)
    private String nickname;

    /**
     * 用户邮箱。
     */
    @Column(length = 100)
    private String email;

    /**
     * 用户手机号。
     */
    @Column(length = 30)
    private String phone;

    /**
     * 账号状态。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /**
     * 用户角色。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 最近一次登录时间。
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间。
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
