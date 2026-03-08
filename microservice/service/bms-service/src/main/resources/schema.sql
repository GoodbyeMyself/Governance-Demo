-- 系统用户表：保存登录账号、角色、状态与审计字段。
CREATE TABLE IF NOT EXISTS sys_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(30) NULL,
    status VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_sys_users PRIMARY KEY (id),
    CONSTRAINT uk_sys_users_username UNIQUE (username),
    CONSTRAINT uk_sys_users_email UNIQUE (email),
    CONSTRAINT uk_sys_users_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 角色表：保留基础 RBAC 结构，便于后续扩展更细粒度授权。
CREATE TABLE IF NOT EXISTS sys_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_sys_roles PRIMARY KEY (id),
    CONSTRAINT uk_sys_roles_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 权限表：当前仅初始化演示所需的权限点。
CREATE TABLE IF NOT EXISTS sys_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_sys_permissions PRIMARY KEY (id),
    CONSTRAINT uk_sys_permissions_code UNIQUE (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 角色与权限关系表：当前用于预留后续 RBAC 能力。
CREATE TABLE IF NOT EXISTS sys_role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_sys_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_roles (id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permissions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 初始化默认角色。
INSERT INTO sys_roles (role_code, role_name)
SELECT 'ADMIN', 'Administrator'
WHERE NOT EXISTS (SELECT 1 FROM sys_roles WHERE role_code = 'ADMIN');

INSERT INTO sys_roles (role_code, role_name)
SELECT 'USER', 'Normal User'
WHERE NOT EXISTS (SELECT 1 FROM sys_roles WHERE role_code = 'USER');

-- 初始化默认权限。
INSERT INTO sys_permissions (permission_code, permission_name)
SELECT 'bms:user:read', 'Read users'
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'bms:user:read');

INSERT INTO sys_permissions (permission_code, permission_name)
SELECT 'bms:user:update-role', 'Update user role'
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'bms:user:update-role');
