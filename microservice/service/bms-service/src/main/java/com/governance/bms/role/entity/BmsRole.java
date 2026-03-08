package com.governance.bms.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色定义实体。
 *
 * <p>该实体映射系统角色表，负责维护角色编码与角色展示名称，
 * 便于后台提供角色定义管理界面。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "sys_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sys_roles_code", columnNames = "role_code")
        }
)
public class BmsRole {

    /**
     * 角色主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色编码。
     */
    @Column(name = "role_code", nullable = false, length = 64)
    private String roleCode;

    /**
     * 角色显示名称。
     */
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;
}
