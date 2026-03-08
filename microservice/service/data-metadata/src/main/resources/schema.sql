-- 元数据采集任务表：保存任务与数据源绑定关系、调度配置和启停状态。
CREATE TABLE IF NOT EXISTS metadata_collection_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    data_source_id BIGINT NOT NULL,
    data_source_name VARCHAR(100) NOT NULL,
    data_source_type VARCHAR(50) NOT NULL,
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
    CONSTRAINT uk_metadata_task_name UNIQUE (task_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
