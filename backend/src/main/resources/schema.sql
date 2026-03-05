CREATE TABLE IF NOT EXISTS data_sources (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    connection_url VARCHAR(500) NULL,
    username VARCHAR(100) NULL,
    password VARCHAR(100) NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_data_sources PRIMARY KEY (id),
    CONSTRAINT uk_data_source_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS metadata_collection_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    data_source_id BIGINT NOT NULL,
    collection_strategy VARCHAR(30) NOT NULL,
    collection_scope VARCHAR(30) NOT NULL,
    target_pattern VARCHAR(500) NULL,
    schedule_type VARCHAR(30) NOT NULL,
    cron_expression VARCHAR(100) NULL,
    config_json VARCHAR(2000) NULL,
    description VARCHAR(500) NULL,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_metadata_collection_tasks PRIMARY KEY (id),
    CONSTRAINT uk_metadata_task_name UNIQUE (task_name),
    CONSTRAINT fk_task_data_source FOREIGN KEY (data_source_id) REFERENCES data_sources (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
