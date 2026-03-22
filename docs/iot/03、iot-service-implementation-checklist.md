# IoT 微服务实施清单（SQL / DTO / API / 包结构）

## 1. 文档目标

本文档是对前面 IoT 文档的进一步细化，目标是为 `iot-device` 与 `iot-collection` 的代码骨架实施提供可直接照着搭建的清单。

本文重点覆盖：
- SQL 草案
- Entity / DTO / Repository / Service / Controller 清单
- API 清单
- 内部接口清单
- Gateway / Nacos / 脚本接入点
- 验证清单

---

## 2. 实施顺序建议

建议按以下顺序实施：

1. 新增两个 Maven 模块：`iot-device`、`iot-collection`
2. 新增两个服务的 `application.yml` 与 `schema.sql`
3. 新增启动类、OpenAPI 配置类、异常处理类
4. 完成 `iot-device` 基础 CRUD 骨架
5. 完成 `iot-collection` 基础任务骨架
6. 接入 Gateway 与 Nacos
7. 接入本地联调脚本与部署脚本
8. 后续再逐步增强业务逻辑

---

## 3. `iot-device` 实施清单

## 3.1 服务目标

`iot-device` 用于承载 IoT 设备主数据与连接配置，第一阶段建议只做最小主表能力。

## 3.2 第一阶段包结构建议

```text
microservice/service/iot-device/
├─ pom.xml
└─ src/main/
   ├─ java/com/governance/
   │  ├─ IotDeviceApplication.java
   │  └─ iotdevice/
   │     ├─ config/
   │     │  └─ OpenApiConfig.java
   │     ├─ controller/
   │     │  ├─ IotDeviceController.java
   │     │  └─ InternalIotDeviceController.java
   │     ├─ dto/
   │     │  ├─ IotDeviceRequest.java
   │     │  └─ IotDeviceResponse.java
   │     ├─ entity/
   │     │  └─ IotDeviceInfo.java
   │     ├─ exception/
   │     │  ├─ DuplicateIotDeviceException.java
   │     │  └─ GlobalExceptionHandler.java
   │     ├─ repository/
   │     │  └─ IotDeviceRepository.java
   │     └─ service/
   │        ├─ IotDeviceService.java
   │        └─ IotDeviceServiceImpl.java
   └─ resources/
      ├─ application.yml
      └─ schema.sql
```

## 3.3 SQL 草案

建议第一阶段使用单表：`iot_devices`

```sql
CREATE TABLE IF NOT EXISTS iot_devices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    device_code VARCHAR(100) NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    protocol_type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(500) NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_iot_devices PRIMARY KEY (id),
    CONSTRAINT uk_iot_device_code UNIQUE (device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 字段说明
- `device_code`：设备编码，唯一
- `device_name`：设备名称
- `device_type`：设备类型，可先用字符串
- `protocol_type`：协议类型，例如 MQTT / HTTP / TCP
- `endpoint`：连接地址或目标地址
- `status`：设备状态，例如 ENABLED / DISABLED / OFFLINE
- `description`：备注说明

## 3.4 Entity 清单

### `IotDeviceInfo`
建议字段：
- `Long id`
- `String deviceCode`
- `String deviceName`
- `String deviceType`
- `String protocolType`
- `String endpoint`
- `String status`
- `String description`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

## 3.5 DTO 清单

### `IotDeviceRequest`
建议字段：
- `deviceCode`
- `deviceName`
- `deviceType`
- `protocolType`
- `endpoint`
- `status`
- `description`

建议校验：
- `deviceCode`：必填，长度限制
- `deviceName`：必填，长度限制
- `deviceType`：必填
- `protocolType`：必填
- `status`：必填
- `endpoint`：长度限制
- `description`：长度限制

### `IotDeviceResponse`
建议字段：
- `id`
- `deviceCode`
- `deviceName`
- `deviceType`
- `protocolType`
- `endpoint`
- `status`
- `description`
- `createdAt`
- `updatedAt`

## 3.6 Repository 清单

### `IotDeviceRepository`
建议方法：
- `boolean existsByDeviceCode(String deviceCode)`
- `boolean existsByDeviceCodeAndIdNot(String deviceCode, Long id)`
- `List<IotDeviceInfo> findAll(Sort sort)`

## 3.7 Service 清单

### `IotDeviceService`
建议方法：
- `IotDeviceResponse createDevice(IotDeviceRequest request)`
- `IotDeviceResponse updateDevice(Long id, IotDeviceRequest request)`
- `void deleteDevice(Long id)`
- `List<IotDeviceResponse> getAllDevices()`
- `IotDeviceResponse getDeviceById(Long id)`

### `IotDeviceServiceImpl`
第一阶段建议业务逻辑：
- 创建时校验 `deviceCode` 唯一
- 更新时校验排除自身后的 `deviceCode` 唯一
- 按 ID 查询时不存在则抛 `ResourceNotFoundException`
- 删除逻辑先保留简单删除，后续再增加采集任务引用校验

## 3.8 Controller 清单

### 对外接口：`IotDeviceController`
建议前缀：
- `/api/iot-device`

建议接口：
- `POST /api/iot-device`
- `PUT /api/iot-device/{id}`
- `DELETE /api/iot-device/{id}`
- `GET /api/iot-device`
- `GET /api/iot-device/{id}`

统一返回：
- `ApiResponse<T>`

### 内部接口：`InternalIotDeviceController`
建议前缀：
- `/internal/iot-devices`

建议接口：
- `GET /internal/iot-devices/{id}`
- `GET /internal/iot-devices`

用于给 `iot-collection` 查询设备详情。

## 3.9 Exception 清单

建议新增：
- `DuplicateIotDeviceException`
- `GlobalExceptionHandler`

异常处理风格应对齐现有 `data-source`。

---

## 4. `iot-collection` 实施清单

## 4.1 服务目标

`iot-collection` 用于承载采集任务主数据，第一阶段先做最小任务骨架。

## 4.2 第一阶段包结构建议

```text
microservice/service/iot-collection/
├─ pom.xml
└─ src/main/
   ├─ java/com/governance/
   │  ├─ IotCollectionApplication.java
   │  └─ iotcollection/
   │     ├─ config/
   │     │  └─ OpenApiConfig.java
   │     ├─ controller/
   │     │  ├─ IotCollectionTaskController.java
   │     │  └─ InternalIotCollectionController.java
   │     ├─ dto/
   │     │  ├─ IotCollectionTaskRequest.java
   │     │  └─ IotCollectionTaskResponse.java
   │     ├─ entity/
   │     │  └─ IotCollectionTask.java
   │     ├─ exception/
   │     │  ├─ DuplicateIotCollectionTaskException.java
   │     │  └─ GlobalExceptionHandler.java
   │     ├─ integration/
   │     │  └─ device/
   │     │     └─ IotDeviceClient.java
   │     ├─ repository/
   │     │  └─ IotCollectionTaskRepository.java
   │     └─ service/
   │        ├─ IotCollectionTaskService.java
   │        └─ IotCollectionTaskServiceImpl.java
   └─ resources/
      ├─ application.yml
      └─ schema.sql
```

## 4.3 SQL 草案

建议第一阶段使用单表：`iot_collection_tasks`

```sql
CREATE TABLE IF NOT EXISTS iot_collection_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    device_id BIGINT NOT NULL,
    device_code VARCHAR(100) NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    collection_type VARCHAR(50) NOT NULL,
    schedule_type VARCHAR(50) NOT NULL,
    cron_expression VARCHAR(100) NULL,
    config_json TEXT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_iot_collection_tasks PRIMARY KEY (id),
    CONSTRAINT uk_iot_collection_task_name UNIQUE (task_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 字段说明
- `task_name`：任务名称，唯一
- `device_id`：设备主键
- `device_code` / `device_name`：冗余存储，便于列表展示
- `collection_type`：采集类型
- `schedule_type`：调度类型
- `cron_expression`：定时表达式
- `config_json`：配置 JSON
- `enabled`：是否启用
- `description`：备注

## 4.4 Entity 清单

### `IotCollectionTask`
建议字段：
- `Long id`
- `String taskName`
- `Long deviceId`
- `String deviceCode`
- `String deviceName`
- `String collectionType`
- `String scheduleType`
- `String cronExpression`
- `String configJson`
- `Boolean enabled`
- `String description`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

## 4.5 DTO 清单

### `IotCollectionTaskRequest`
建议字段：
- `taskName`
- `deviceId`
- `collectionType`
- `scheduleType`
- `cronExpression`
- `configJson`
- `enabled`
- `description`

建议校验：
- `taskName`：必填
- `deviceId`：必填
- `collectionType`：必填
- `scheduleType`：必填
- `enabled`：必填
- `cronExpression`：长度限制
- `description`：长度限制

### `IotCollectionTaskResponse`
建议字段：
- `id`
- `taskName`
- `deviceId`
- `deviceCode`
- `deviceName`
- `collectionType`
- `scheduleType`
- `cronExpression`
- `configJson`
- `enabled`
- `description`
- `createdAt`
- `updatedAt`

## 4.6 Repository 清单

### `IotCollectionTaskRepository`
建议方法：
- `boolean existsByTaskName(String taskName)`
- `boolean existsByTaskNameAndIdNot(String taskName, Long id)`
- `long countByDeviceId(Long deviceId)`
- `List<IotCollectionTask> findAll(Sort sort)`

## 4.7 Service 清单

### `IotCollectionTaskService`
建议方法：
- `IotCollectionTaskResponse createTask(IotCollectionTaskRequest request)`
- `IotCollectionTaskResponse updateTask(Long id, IotCollectionTaskRequest request)`
- `void deleteTask(Long id)`
- `List<IotCollectionTaskResponse> getAllTasks()`
- `IotCollectionTaskResponse getTaskById(Long id)`
- `long countByDeviceId(Long deviceId)`

### `IotCollectionTaskServiceImpl`
第一阶段建议逻辑：
- 创建时校验任务名唯一
- 调用 `IotDeviceClient` 校验设备存在
- 将设备 `deviceCode/deviceName` 回填到任务记录中
- 更新时校验任务名唯一
- 列表按 `id ASC` 或 `updatedAt DESC` 返回

## 4.8 Controller 清单

### 对外接口：`IotCollectionTaskController`
建议前缀：
- `/api/iot-collection/tasks`

建议接口：
- `POST /api/iot-collection/tasks`
- `PUT /api/iot-collection/tasks/{id}`
- `DELETE /api/iot-collection/tasks/{id}`
- `GET /api/iot-collection/tasks`
- `GET /api/iot-collection/tasks/{id}`

### 内部接口：`InternalIotCollectionController`
建议前缀：
- `/internal/iot-collection/tasks`

建议接口：
- `GET /internal/iot-collection/tasks/count-by-device/{deviceId}`

该接口主要给 `iot-device` 删除前做引用校验预留。

## 4.9 Integration 清单

### `IotDeviceClient`
建议：
- 使用 OpenFeign
- 调用 `iot-device` 的 `/internal/iot-devices/{id}`

建议路径：
- `com.governance.iotcollection.integration.device.IotDeviceClient`

## 4.10 Exception 清单

建议新增：
- `DuplicateIotCollectionTaskException`
- `GlobalExceptionHandler`

异常处理风格应对齐现有 `data-metadata`。

---

## 5. Gateway / Nacos / 脚本接入清单

## 5.1 Maven 父模块注册
修改：
- `microservice/pom.xml`

新增：
- `service/iot-device`
- `service/iot-collection`

## 5.2 Gateway 路由
修改：
- `microservice/nacos-config/gateway.yaml`
- `microservice/gateway/src/main/resources/application.yml`

新增：
- `/api/iot-device/** -> lb://iot-device`
- `/api/iot-collection/** -> lb://iot-collection`
- 两个服务的 Swagger 路由
- Swagger 聚合 `urls`

## 5.3 Nacos 配置
新增：
- `microservice/nacos-config/iot-device.yaml`
- `microservice/nacos-config/iot-collection.yaml`

## 5.4 本地联调脚本
修改：
- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`

建议端口：
- `iot-device`：8085
- `iot-collection`：8086

## 5.5 部署脚本
修改：
- `microservice/scripts/docker-centos9/service-manager.sh`
- `microservice/scripts/docker-centos9/init-single-container.ps1`
- `microservice/scripts/docker-centos9/update-single-container.ps1`

---

## 6. 验证清单

## 6.1 文档检查
- SQL 草案是否足够支持第一阶段骨架
- DTO 清单是否完整
- API 清单是否完整
- 接入点是否明确

## 6.2 代码骨架检查
- `iot-device` 模块是否创建完成
- `iot-collection` 模块是否创建完成
- 两边 `application.yml` / `schema.sql` 是否存在
- Gateway 是否配置完成
- Nacos 是否配置完成
- 本地脚本是否接入完成
- 部署脚本是否识别新服务

## 6.3 后续可执行验证
如果允许执行构建，可进一步检查：
- `cd microservice && mvn -DskipTests package`
- 健康检查脚本是否识别 8085 / 8086
- Swagger 聚合页是否出现两个 IoT 服务

---

## 7. 关键参考文件

### `iot-device` 模板参考
- `microservice/service/data-source/pom.xml`
- `microservice/service/data-source/src/main/resources/application.yml`
- `microservice/service/data-source/src/main/resources/schema.sql`
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/DataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/InternalDataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/GlobalExceptionHandler.java`

### `iot-collection` 模板参考
- `microservice/service/data-metadata/pom.xml`
- `microservice/service/data-metadata/src/main/resources/application.yml`
- `microservice/service/data-metadata/src/main/resources/schema.sql`
- `microservice/service/data-metadata/src/main/java/com/governance/metadata/controller/MetadataCollectionTaskController.java`
- `microservice/service/data-metadata/src/main/java/com/governance/metadata/controller/InternalMetadataController.java`
- `microservice/service/data-metadata/src/main/java/com/governance/metadata/integration/datasource/DataSourceClient.java`
- `microservice/service/data-metadata/src/main/java/com/governance/metadata/exception/GlobalExceptionHandler.java`
