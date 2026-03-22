# IoT 设备管理与设备采集前后端设计说明

## 1. 文档目的

本文档用于说明在当前 `Governance-Demo` 工程中，如何设计“物联网设备管理”和“设备采集”能力，包括：
- 服务边界如何拆分
- 后端数据库如何设计
- 接口如何分组
- 前端页面如何组织
- 如何接入当前网关、Nacos、本地脚本与前端管理台

本文档基于当前代码库现状编写，不是通用方案文档。

---

## 2. 当前工程架构映射

## 2.1 后端现状

当前后端采用 Spring Boot 多模块 Maven 结构，关键参考如下：
- `microservice/pom.xml`
- `microservice/README.md`
- `microservice/nacos-config/gateway.yaml`

当前服务边界：
- `auth-center`
- `bms-service`
- `data-source`
- `data-metadata`

已知模式：
- 服务注册：Nacos Discovery
- 服务配置：Nacos Config
- 对外路由：Gateway
- 持久化：Spring Data JPA + MySQL
- 外部接口：`/api/**`
- 内部接口：`/internal/**`
- 对外响应：`ApiResponse<T>`

## 2.2 前端现状

当前后台管理端位于：
- `web/apps/govern`

关键参考如下：
- `web/apps/govern/src/App.tsx`
- `web/packages/ui/layouts/GovernanceAppShell.tsx`
- `web/packages/utils/auth/routes.ts`
- `web/apps/govern/src/features/data-source/index.tsx`
- `web/apps/govern/src/features/metadata-collection/index.tsx`

已知模式：
- 页面按领域划分在 `features/*`
- API 包按领域划分在 `packages/api/*`
- 页面状态管理以本地 `useState/useEffect` 为主
- 列表/创建/编辑模式参考 `data-source`
- 任务/详情模式参考 `metadata-collection`

---

## 3. 推荐服务边界

## 3.1 推荐拆分方案

建议将 IoT 领域拆成两个微服务：

### 服务 1：`iot-device`
负责：
- 设备产品模型
- 协议类型配置
- 设备主数据
- 设备连接配置
- 设备启停状态
- 在线/离线状态
- 设备详情

### 服务 2：`iot-collection`
负责：
- 采集任务配置
- 设备最新数据
- 设备历史数据
- 设备事件记录
- 采集概览统计

## 3.2 为什么这样拆

此拆分方式与当前系统已有边界一致：
- `data-source`：主数据
- `data-metadata`：采集任务

IoT 场景下：
- `iot-device` 对应“设备主数据”
- `iot-collection` 对应“设备采集运行与结果”

好处：
1. 与现有项目认知模型一致
2. 便于后续独立扩展采集侧能力
3. 降低设备管理与实时数据处理的耦合
4. 更适合未来引入异步处理、消息中间件、时序存储

---

## 4. 后端服务设计

## 4.1 `iot-device` 服务设计

### 4.1.1 目标职责

`iot-device` 应负责：
- 产品模型 CRUD
- 设备 CRUD
- 设备连接配置管理
- 设备连接测试
- 设备启用/禁用
- 输出设备详情与基础状态
- 对 `iot-collection` 提供内部查询能力

### 4.1.2 建议目录结构

建议延续当前服务结构：

```text
microservice/service/iot-device/
├─ pom.xml
└─ src/main/
   ├─ java/com/governance/iotdevice/
   │  ├─ controller/
   │  ├─ service/
   │  ├─ repository/
   │  ├─ entity/
   │  ├─ dto/
   │  ├─ exception/
   │  └─ config/
   └─ resources/
      ├─ application.yml
      └─ schema.sql
```

### 4.1.3 建议实体

#### 产品模型实体
建议实体：`IotProduct`

建议字段：
- `id`
- `productKey`
- `name`
- `protocolType`
- `dataFormat`
- `description`
- `enabled`
- `createdAt`
- `updatedAt`

#### 产品字段实体
建议实体：`IotProductField`

建议字段：
- `id`
- `productId`
- `fieldCode`
- `fieldName`
- `fieldType`
- `unit`
- `required`
- `sortOrder`
- `createdAt`
- `updatedAt`

#### 设备实体
建议实体：`IotDevice`

建议字段：
- `id`
- `deviceCode`
- `productId`
- `name`
- `protocolType`
- `connectionHost`
- `connectionPort`
- `topicOrPath`
- `username`
- `passwordOrSecret`
- `status`
- `onlineStatus`
- `lastOnlineAt`
- `lastOfflineAt`
- `description`
- `createdAt`
- `updatedAt`

### 4.1.4 建议接口分组

#### 产品模型对外接口
- `POST /api/iot-device/products`
- `PUT /api/iot-device/products/{id}`
- `DELETE /api/iot-device/products/{id}`
- `GET /api/iot-device/products`
- `GET /api/iot-device/products/{id}`

#### 设备对外接口
- `POST /api/iot-device/devices`
- `PUT /api/iot-device/devices/{id}`
- `DELETE /api/iot-device/devices/{id}`
- `GET /api/iot-device/devices`
- `GET /api/iot-device/devices/{id}`

#### 连接与状态接口
- `POST /api/iot-device/devices/{id}/connect-test`
- `POST /api/iot-device/devices/{id}/enable`
- `POST /api/iot-device/devices/{id}/disable`

#### 内部接口
- `GET /internal/iot-devices/{id}`
- `GET /internal/iot-devices`
- `GET /internal/iot-devices/{id}/collection-profile`

### 4.1.5 业务规则建议

- 删除产品模型前，需校验是否被设备引用
- 删除设备前，需校验是否被采集任务引用
- 新增/修改设备时，需校验产品模型是否存在
- 禁用设备后，不允许再被新建采集任务使用
- 连接测试失败时，不影响设备保存，但应提示状态

---

## 4.2 `iot-collection` 服务设计

### 4.2.1 目标职责

`iot-collection` 应负责：
- 采集任务 CRUD
- 设备最新采集值维护
- 历史数据存储与查询
- 事件记录存储与查询
- 输出采集统计概览

### 4.2.2 建议目录结构

```text
microservice/service/iot-collection/
├─ pom.xml
└─ src/main/
   ├─ java/com/governance/iotcollection/
   │  ├─ controller/
   │  ├─ service/
   │  ├─ repository/
   │  ├─ entity/
   │  ├─ dto/
   │  ├─ exception/
   │  ├─ integration/
   │  └─ config/
   └─ resources/
      ├─ application.yml
      └─ schema.sql
```

建议参考 `data-metadata` 的 Feign 调用方式，内部通过 `iot-device` 的 `/internal/**` 接口校验设备信息。

### 4.2.3 建议实体

#### 采集任务实体
建议实体：`IotCollectionTask`

字段：
- `id`
- `taskName`
- `deviceId`
- `collectionMode`
- `scheduleType`
- `cronExpression`
- `enabled`
- `configJson`
- `description`
- `createdAt`
- `updatedAt`

#### 最新值实体
建议实体：`IotDeviceTelemetryLatest`

字段：
- `id`
- `deviceId`
- `metricCode`
- `metricName`
- `valueText`
- `valueNumber`
- `valueType`
- `unit`
- `collectedAt`
- `updatedAt`

#### 历史值实体
建议实体：`IotDeviceTelemetryHistory`

字段：
- `id`
- `deviceId`
- `metricCode`
- `valueText`
- `valueNumber`
- `valueType`
- `unit`
- `collectedAt`
- `sourceType`
- `rawPayloadId`

#### 事件实体
建议实体：`IotDeviceEvent`

字段：
- `id`
- `deviceId`
- `eventType`
- `eventCode`
- `level`
- `eventTime`
- `message`
- `rawPayloadId`

#### 原始报文实体
建议实体：`IotRawPayload`

字段：
- `id`
- `deviceId`
- `protocolType`
- `payloadBody`
- `receivedAt`
- `parseStatus`
- `errorMessage`

### 4.2.4 建议接口分组

#### 采集任务接口
- `POST /api/iot-collection/tasks`
- `PUT /api/iot-collection/tasks/{id}`
- `DELETE /api/iot-collection/tasks/{id}`
- `GET /api/iot-collection/tasks`
- `GET /api/iot-collection/tasks/{id}`

#### 实时数据接口
- `GET /api/iot-collection/telemetry/latest?deviceId=`
- `GET /api/iot-collection/telemetry/history?deviceId=&metricCode=&start=&end=`

#### 事件接口
- `GET /api/iot-collection/events?deviceId=&eventType=&start=&end=`

#### 概览接口
- `GET /api/iot-collection/dashboard/overview`
- `GET /api/iot-collection/dashboard/device-trend`
- `GET /api/iot-collection/dashboard/metric-summary`

#### 内部接收接口（预留）
- `POST /internal/iot-collection/ingest/{deviceCode}`
- `POST /internal/iot-collection/events/{deviceCode}`

### 4.2.5 业务规则建议

- 新增采集任务前必须校验设备是否存在
- 若设备被禁用，则不允许创建新的启用中采集任务
- 删除设备前，需先校验或删除相关采集任务
- 最新值与历史值应同时维护，但职责区分清晰
- 历史值查询必须支持时间范围筛选

---

## 5. 数据库设计建议

## 5.1 `iot-device` 数据库

建议数据库名：`iot_device_db`

建议核心表：
- `iot_products`
- `iot_product_fields`
- `iot_devices`
- `iot_device_tags`（可选）
- `iot_device_groups`（可选）
- `iot_device_group_rel`（可选）
- `iot_device_commands`（可选）

## 5.2 `iot-collection` 数据库

建议数据库名：`iot_collection_db`

建议核心表：
- `iot_collection_tasks`
- `iot_device_telemetry_latest`
- `iot_device_telemetry_history`
- `iot_device_events`
- `iot_raw_payloads`

## 5.3 存储策略建议

结合当前工程结构，第一阶段建议：
- 继续使用 MySQL + JPA
- `latest` 与 `history` 表分离
- 先满足管理与查询诉求

文档中应明确：
- 第一阶段的“实时”定义为“近实时入库与查询”
- 后续若数据规模显著上涨，再考虑时序数据库或流式基础设施

---

## 6. 服务间协作设计

## 6.1 推荐调用关系

推荐：
- `iot-collection` 调用 `iot-device` 的内部接口
- `iot-device` 不直接依赖 `iot-collection` 的运行能力

即保持依赖方向单向化：
- 设备主数据服务提供基础画像
- 采集服务消费该画像

## 6.2 内部调用场景

`iot-collection` 在以下场景调用 `iot-device`：
- 新建/编辑采集任务时校验设备是否存在
- 读取设备协议类型和产品模型信息
- 获取设备是否启用/在线状态

## 6.3 删除校验策略

建议：
- 删除设备时，由 `iot-device` 通过内部调用或反查方式判断是否存在采集任务引用
- 如存在引用，返回 409

此策略与当前 `data-source` / `data-metadata` 的互相引用校验方式一致。

---

## 7. 网关与注册中心设计

## 7.1 Nacos 注册

需要新增：
- `microservice/nacos-config/iot-device.yaml`
- `microservice/nacos-config/iot-collection.yaml`

每个服务的 `application.yml` 应包含：
- `spring.application.name`
- `spring.config.import: optional:nacos:${spring.application.name}.yaml`
- `spring.cloud.nacos.discovery.server-addr`
- `spring.cloud.nacos.config.server-addr`

## 7.2 Gateway 路由

需要修改：
- `microservice/nacos-config/gateway.yaml`

应新增：
- `/api/iot-device/** -> lb://iot-device`
- `/api/iot-collection/** -> lb://iot-collection`

Swagger 路由建议新增：
- `/iot-device/swagger-ui.html`
- `/iot-device/v3/api-docs`
- `/iot-collection/swagger-ui.html`
- `/iot-collection/v3/api-docs`

Swagger 聚合列表也应新增两个入口。

---

## 8. 前端页面设计

## 8.1 页面组织建议

建议新增以下前端 feature：

### IoT 设备域
- `web/apps/govern/src/features/iot-device/index.tsx`
- `web/apps/govern/src/features/iot-device/detail/index.tsx`
- `web/apps/govern/src/features/iot-product/index.tsx`

### IoT 采集域
- `web/apps/govern/src/features/iot-collection/index.tsx`
- `web/apps/govern/src/features/iot-collection/detail/index.tsx`
- `web/apps/govern/src/features/iot-device-telemetry/index.tsx`

## 8.2 页面职责建议

### 设备列表页
参考 `data-source/index.tsx`

建议具备：
- 筛选区
- 创建设备按钮
- 编辑按钮
- 删除按钮
- 启用/禁用按钮
- 连接测试按钮
- 在线状态标签

### 产品模型页
参考 `data-source` 的 CRUD 风格

建议具备：
- 产品列表
- 字段模型配置
- 新增/编辑模态框

### 采集任务页
参考 `metadata-collection/index.tsx`

建议具备：
- 任务表格
- 关联设备下拉
- 调度方式
- 启用开关
- 详情入口

### 设备数据页
建议具备：
- 当前指标卡片
- 历史数据表格
- 时间范围筛选
- 事件列表

## 8.3 路由建议

建议在 `web/packages/utils/auth/routes.ts` 中新增：
- `IOT_DEVICE_PATH`
- `IOT_PRODUCT_PATH`
- `IOT_COLLECTION_PATH`
- `IOT_DEVICE_TELEMETRY_PATH`
- `buildIotDeviceDetailPath(id)`
- `buildIotCollectionDetailPath(id)`

并在 `web/apps/govern/src/App.tsx` 中新增对应 Route。

## 8.4 菜单建议

在 `web/packages/ui/layouts/GovernanceAppShell.tsx` 中新增：
- 设备管理
- 产品模型
- 设备采集

## 8.5 API 包建议

建议新增：
- `web/packages/api/iot-device/`
- `web/packages/api/iot-collection/`

内部结构延续现有模式：
- `types.ts`
- `service.ts`
- `index.ts`

并在 `web/packages/api/index.ts` 中统一导出。

---

## 9. 前后端映射关系建议

## 9.1 页面与后端接口映射

### 设备管理页
对应后端：
- `/api/iot-device/devices`
- `/api/iot-device/devices/{id}`
- `/api/iot-device/devices/{id}/connect-test`

### 产品模型页
对应后端：
- `/api/iot-device/products`
- `/api/iot-device/products/{id}`

### 采集任务页
对应后端：
- `/api/iot-collection/tasks`
- `/api/iot-collection/tasks/{id}`

### 设备数据页
对应后端：
- `/api/iot-collection/telemetry/latest`
- `/api/iot-collection/telemetry/history`
- `/api/iot-collection/events`

## 9.2 页面数据加载模式

建议完全延续现有实现风格：
- `useState` 保存列表、明细、loading
- `useEffect` 首次加载
- `message.useMessage()` 提示操作结果
- 变更后刷新列表

不建议为了新模块单独引入新状态管理框架。

---

## 10. 分阶段实施建议

## 10.1 Phase 1

优先落地：
- `iot-device` 基础 CRUD
- `iot-collection` 任务 CRUD
- 最新值/历史值查询
- Gateway/Nacos/Swagger 接入
- govern 前端页面接入

## 10.2 Phase 2

增强：
- 更真实的协议连接测试
- 设备在线状态刷新机制
- 采集事件与错误解析增强
- 概览统计增强

## 10.3 Phase 3

演进：
- 告警
- 指令下发
- 时序数据库
- 消息驱动采集

---

## 11. 验证建议

后续真正实现时，建议按以下方式验证：

1. 新服务是否成功注册到 Nacos
2. Gateway 是否可转发新服务路由
3. Swagger 聚合页是否出现新服务文档
4. 前端新菜单是否能正常访问
5. 设备 CRUD 是否可用
6. 采集任务 CRUD 是否可用
7. 最新数据与历史数据接口是否可返回结果
8. 删除设备/产品模型时引用校验是否生效

---

## 12. 关键参考文件

### 后端参考
- `microservice/pom.xml`
- `microservice/README.md`
- `microservice/nacos-config/gateway.yaml`
- `microservice/service/data-source/**`
- `microservice/service/data-metadata/**`

### 前端参考
- `web/apps/govern/src/App.tsx`
- `web/packages/ui/layouts/GovernanceAppShell.tsx`
- `web/packages/utils/auth/routes.ts`
- `web/apps/govern/src/features/data-source/index.tsx`
- `web/apps/govern/src/features/metadata-collection/index.tsx`

结论上，本需求在当前工程中的最佳落地方式是：
- 后端拆分为 `iot-device` + `iot-collection`
- 前端分别提供设备管理、产品模型、采集任务、设备数据页面
- 整体接入方式严格遵循当前已有微服务和 govern 管理台模式
