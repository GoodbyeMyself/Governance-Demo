-- 数据源主表：保存数据源类型、连接信息与描述字段。
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
