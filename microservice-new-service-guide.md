# 在当前工程中新增一个微服务的详细步骤说明

## 1. 文档目标

本文档用于说明：在当前 `Governance-Demo` 工程中，如何新增一个完整可运行、可注册、可通过网关访问、可被前端接入的微服务。

本文不是通用 Spring Boot 教程，而是**严格基于当前仓库结构**总结的操作步骤。

适用场景包括但不限于：
- 新增 `iot-device`
- 新增 `iot-collection`
- 后续新增其他治理相关业务服务

---

## 2. 当前工程的微服务组成方式

当前后端工程根目录：
- `microservice/`

关键文件：
- `microservice/pom.xml`
- `microservice/README.md`
- `microservice/nacos-config/gateway.yaml`
- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`
- `microservice/scripts/docker-centos9/*`

当前已存在的服务模块：
- `service/auth-center`
- `service/bms-service`
- `service/data-source`
- `service/data-metadata`

所以在当前工程中，“新增一个微服务”并不只是新建一个 Spring Boot 模块，而是至少需要同时处理：

1. Maven 模块注册
2. 服务代码骨架
3. `application.yml` 与数据库配置
4. Nacos 配置文件
5. Gateway 路由与 Swagger 聚合
6. 本地联调脚本接入
7. 单容器部署脚本接入
8. 前端 API 与页面接入

---

## 3. 第一步：明确新微服务的边界

在动手之前，先定义清楚这个微服务负责什么、不负责什么。

例如 IoT 场景中：
- `iot-device` 负责设备主数据与连接配置
- `iot-collection` 负责采集任务与数据

建议判断维度：
- 是否拥有独立业务主数据
- 是否需要独立数据库
- 是否会被其他服务调用
- 是否需要内部接口 `/internal/**`
- 是否需要单独挂到网关下暴露给前端

如果答案大多为“是”，则适合独立微服务。

---

## 4. 第二步：在 Maven 中注册新模块

需要修改文件：
- `microservice/pom.xml`

当前该文件通过 `<modules>` 管理所有微服务模块。

### 操作方法
在 `<modules>` 中新增你的服务目录，例如：

```xml
<module>service/iot-device</module>
<module>service/iot-collection</module>
```

### 说明
只有注册到父 POM 后：
- `mvn -DskipTests package` 才会构建这个模块
- 本地多模块依赖关系才会被 Maven 正确识别

---

## 5. 第三步：新建服务目录与基础骨架

建议新服务目录放在：
- `microservice/service/<service-name>`

例如：
- `microservice/service/iot-device`
- `microservice/service/iot-collection`

### 推荐做法
优先复制当前最相近的服务结构作为模板：

#### 如果是主数据/CRUD 型服务
建议参考：
- `microservice/service/data-source`

#### 如果是任务/采集/内部调用型服务
建议参考：
- `microservice/service/data-metadata`

### 建议的标准结构

```text
microservice/service/<service-name>/
├─ pom.xml
└─ src/main/
   ├─ java/com/governance/<domain>/
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

建议根包统一仍使用：
- `com.governance`

---

## 6. 第四步：编写新服务的 `pom.xml`

### 6.1 推荐复制模板
优先参考：
- `microservice/service/data-source/pom.xml`
- `microservice/service/data-metadata/pom.xml`

### 6.2 常见依赖
一个典型业务服务通常需要：
- `service-support`
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-security`
- `springdoc-openapi-starter-webmvc-ui`
- `spring-cloud-starter-alibaba-nacos-discovery`
- `spring-cloud-starter-alibaba-nacos-config`
- `mysql-connector-j`

如果涉及服务间调用，再加：
- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-loadbalancer`

### 6.3 如何判断是否要 Feign
如果你的服务需要调用其他服务的 `/internal/**` 接口，例如：
- `iot-collection` 需要查 `iot-device`

则应启用 Feign。

---

## 7. 第五步：新增 Spring Boot 启动类

需要在新服务中新增启动类，例如：
- `IotDeviceApplication.java`
- `IotCollectionApplication.java`

### 参考文件
- `microservice/service/data-source/src/main/java/com/governance/DataSourceApplication.java`
- `microservice/service/data-metadata/src/main/java/com/governance/DataMetadataApplication.java`

### 注意事项
如果服务需要 Feign，则在启动类上加：
- `@EnableFeignClients`

---

## 8. 第六步：编写 `application.yml`

每个服务都应有自己的：
- `src/main/resources/application.yml`

### 必备配置项
建议至少包含：

```yaml
server:
  port: 8085

spring:
  application:
    name: iot-device
  config:
    import: optional:nacos:${spring.application.name}.yaml
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/iot_device_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
  sql:
    init:
      mode: always

security:
  gateway:
    trusted-token: ${GATEWAY_TRUSTED_TOKEN:change-me-gateway-token}

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 参考文件
- `microservice/service/data-source/src/main/resources/application.yml`
- `microservice/service/data-metadata/src/main/resources/application.yml`

---

## 9. 第七步：新增 `schema.sql`

如果服务拥有自己的数据库表，应新增：
- `src/main/resources/schema.sql`

### 作用
- 初始化基础表结构
- 结合 `spring.sql.init.mode: always` 在启动时执行

### 参考文件
- `microservice/service/data-source/src/main/resources/schema.sql`
- `microservice/service/data-metadata/src/main/resources/schema.sql`

### 建议
- 保持当前项目命名风格：小写下划线
- 主键约束、唯一约束命名清晰
- 第一阶段尽量不引入复杂外键，优先保持与现有服务一致的风格

---

## 10. 第八步：新增 Nacos 配置文件

需要新增文件：
- `microservice/nacos-config/<service-name>.yaml`

例如：
- `microservice/nacos-config/iot-device.yaml`
- `microservice/nacos-config/iot-collection.yaml`

### 作用
该目录下的配置会在当前部署脚本/启动流程中被加载到 Nacos。

### 配置内容建议
可包含：
- 数据库连接覆盖项
- 端口覆盖项
- 服务内部调用配置
- Swagger 配置
- 自定义业务配置

### 参考文件
- `microservice/nacos-config/data-source.yaml`
- `microservice/nacos-config/data-metadata.yaml`

---

## 11. 第九步：将服务注册到 Gateway

需要修改文件：
- `microservice/nacos-config/gateway.yaml`

### 11.1 添加业务路由
例如新增：

```yaml
- id: iot-device
  uri: lb://iot-device
  predicates:
    - Path=/api/iot-device/**

- id: iot-collection
  uri: lb://iot-collection
  predicates:
    - Path=/api/iot-collection/**
```

### 11.2 添加 Swagger 路由
例如新增：

```yaml
- id: iot-device-swagger
  uri: lb://iot-device
  predicates:
    - Path=/iot-device/swagger-ui.html,/iot-device/swagger-ui/**,/iot-device/v3/api-docs,/iot-device/v3/api-docs/**
  filters:
    - StripPrefix=1

- id: iot-collection-swagger
  uri: lb://iot-collection
  predicates:
    - Path=/iot-collection/swagger-ui.html,/iot-collection/swagger-ui/**,/iot-collection/v3/api-docs,/iot-collection/v3/api-docs/**
  filters:
    - StripPrefix=1
```

### 11.3 添加 Swagger 聚合入口
在 `springdoc.swagger-ui.urls` 中新增两项，使聚合 Swagger 页面可显示新服务。

### 参考文件
- `microservice/nacos-config/gateway.yaml`

---

## 12. 第十步：设计服务接口

### 12.1 外部接口规范
建议：
- 路径前缀为 `/api/<domain>`
- 返回统一 `ApiResponse<T>`
- 使用 `@Tag` 和 `@Operation`

例如：
- `/api/iot-device/devices`
- `/api/iot-collection/tasks`

### 12.2 内部接口规范
建议：
- 路径前缀为 `/internal/<domain>`
- 供其他微服务通过 Feign 调用
- 通常可返回原始 DTO，而不是 `ApiResponse<T>`

例如：
- `/internal/iot-devices/{id}`
- `/internal/iot-devices/{id}/collection-profile`

### 12.3 设计原则
- 主数据类接口参考 `data-source`
- 任务/详情/统计类接口参考 `data-metadata`
- 删除前做引用校验
- 外部 API 与内部 API 分离

---

## 13. 第十一步：接入安全与统一异常风格

### 13.1 安全接入
当前共享安全逻辑已在：
- `common/service-support`

所以新服务只要依赖 `service-support`，并保持配置项一致，即可复用：
- 网关透传头
- 认证上下文
- `/internal/**` 放行规则
- `actuator/health` 放行规则

### 13.2 异常处理
建议为每个新服务新增：
- 领域异常类
- `GlobalExceptionHandler`

并对齐当前服务风格：
- 400 参数错误
- 404 资源不存在
- 409 冲突
- 500 兜底异常

### 参考文件
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/GlobalExceptionHandler.java`

---

## 14. 第十二步：接入本地联调脚本

需要修改：
- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`

### 14.1 修改 `start-local.ps1`
要做的事情：
- 给新服务分配端口
- 将新服务加入 `$services` 列表
- 设定服务目录
- 设定启动顺序

### 14.2 修改 `health-check.ps1`
要做的事情：
- 为新服务增加 `http://localhost:<port>/actuator/health` 检查

### 14.3 端口建议
建议在现有基础上顺延分配，例如：
- `iot-device` -> 8085
- `iot-collection` -> 8086

最终以实际未占用端口为准。

---

## 15. 第十三步：接入单容器部署脚本

需要评估并修改以下文件：
- `microservice/scripts/docker-centos9/init-single-container.ps1`
- `microservice/scripts/docker-centos9/update-single-container.ps1`
- `microservice/scripts/docker-centos9/service-manager.sh`
- `DEPLOYMENT.md`

### 需要处理的事项
1. 新服务 jar 是否打包并复制到容器
2. 容器启动时是否拉起新服务
3. 是否增加数据库初始化
4. 是否增加服务管理命令支持
5. 是否更新部署文档中的访问地址与端口说明

### 参考说明
当前工程把“新增一个微服务”视为完整平台能力新增，因此部署层也必须同步更新，而不是只改后端代码。

---

## 16. 第十四步：前端 API 层接入

需要在前端新增：
- `web/packages/api/<domain>/`

例如：
- `web/packages/api/iot-device/`
- `web/packages/api/iot-collection/`

### 推荐结构

```text
web/packages/api/iot-device/
├─ service.ts
├─ types.ts
└─ index.ts
```

### 说明
- `types.ts`：声明请求/响应类型
- `service.ts`：封装 HTTP 调用
- `index.ts`：导出 API

### 参考文件
- `web/packages/api/data-source/service.ts`
- `web/packages/api/metadata-collection/service.ts`

并在：
- `web/packages/api/index.ts`
中统一导出。

---

## 17. 第十五步：前端页面接入

### 17.1 新增页面
建议在：
- `web/apps/govern/src/features/`

下增加新页面目录，例如：
- `iot-device/`
- `iot-product/`
- `iot-collection/`
- `iot-device-telemetry/`

### 17.2 接入路由
需要修改：
- `web/apps/govern/src/App.tsx`
- `web/packages/utils/auth/routes.ts`

在 `routes.ts` 中新增路径常量，例如：
- `IOT_DEVICE_PATH`
- `IOT_PRODUCT_PATH`
- `IOT_COLLECTION_PATH`

在 `App.tsx` 中新增 Route。

### 17.3 接入左侧菜单
需要修改：
- `web/packages/ui/layouts/GovernanceAppShell.tsx`

新增导航项，例如：
- 设备管理
- 产品模型
- 设备采集

### 17.4 国际化
需要修改：
- `web/packages/i18n/locales/zh-CN/*`
- `web/packages/i18n/locales/en-US/*`

至少新增：
- 导航文案
- 模块标题
- 按钮文案
- 表单标签

---

## 18. 第十六步：Swagger 与文档接入

每个新服务建议补齐：
- OpenAPI 配置
- Controller 层 Swagger 注解

这样既能单服务查看文档，也能被网关聚合展示。

### 参考文件
- 当前各服务中的 OpenAPI 配置与 Controller 注解风格

最终应验证：
- `http://localhost:8080/swagger-ui.html` 聚合页能看到新服务
- 新服务 Swagger 路径能正常打开

---

## 19. 第十七步：验证新增微服务是否真正完成接入

新增微服务完成后，至少要做以下验证：

### 19.1 构建验证
在 `microservice/` 下执行：

```powershell
mvn -DskipTests package
```

确认新模块参与构建且无报错。

### 19.2 本地启动验证
执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1
```

确认新服务被拉起。

### 19.3 健康检查验证
执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

确认新服务健康检查通过。

### 19.4 Gateway 验证
确认通过网关可访问：
- `/api/<domain>/**`

### 19.5 Swagger 验证
确认聚合 Swagger 页中出现新服务。

### 19.6 前端验证
确认：
- 左侧菜单出现新模块
- 页面可进入
- 页面可正常调用新 API

---

## 20. 新增微服务的最小落地清单

如果你要快速判断“一个微服务是否真的新增完整了”，至少确认以下 10 项：

1. `microservice/pom.xml` 已注册模块
2. `microservice/service/<service-name>/` 已创建
3. 新服务 `pom.xml` 已可构建
4. `application.yml` 已配置
5. `schema.sql` 已配置
6. `microservice/nacos-config/<service-name>.yaml` 已新增
7. `microservice/nacos-config/gateway.yaml` 已加路由和 Swagger 聚合
8. `start-local.ps1` 已接入
9. 部署脚本已接入
10. 前端页面/API 已接入

只完成其中一部分，不能算真正完成了“新增微服务”。

---

## 21. IoT 场景下的推荐新增方式

如果本次是为 IoT 场景新增服务，推荐按如下顺序：

1. 新增 `iot-device`
2. 新增 `iot-collection`
3. 先打通管理与查询能力
4. 再逐步增强协议接入与实时处理能力

原因：
- 更贴合当前仓库现有微服务拆分模式
- 更容易先跑通 CRUD、管理台、网关和脚本接入
- 后续扩展不会把单个服务做得过重

---

## 22. 关键参考文件清单

### 后端模块与配置
- `microservice/pom.xml`
- `microservice/README.md`
- `microservice/service/data-source/pom.xml`
- `microservice/service/data-metadata/pom.xml`
- `microservice/service/data-source/src/main/resources/application.yml`
- `microservice/service/data-metadata/src/main/resources/application.yml`
- `microservice/service/data-source/src/main/resources/schema.sql`
- `microservice/service/data-metadata/src/main/resources/schema.sql`

### 网关与注册
- `microservice/nacos-config/gateway.yaml`
- `microservice/nacos-config/data-source.yaml`
- `microservice/nacos-config/data-metadata.yaml`

### 本地与部署脚本
- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`
- `microservice/scripts/docker-centos9/init-single-container.ps1`
- `microservice/scripts/docker-centos9/update-single-container.ps1`
- `microservice/scripts/docker-centos9/service-manager.sh`
- `DEPLOYMENT.md`

### 前端接入
- `web/apps/govern/src/App.tsx`
- `web/packages/ui/layouts/GovernanceAppShell.tsx`
- `web/packages/utils/auth/routes.ts`
- `web/packages/api/data-source/service.ts`
- `web/packages/api/metadata-collection/service.ts`

---

## 23. 总结

在当前工程中新增一个微服务，必须把它当作“平台一级能力”来接入，而不是只创建一个 Spring Boot 模块即可。

正确做法是同时完成：
- 后端模块注册
- 数据库与配置初始化
- Nacos 配置
- Gateway 路由与 Swagger 聚合
- 本地联调脚本接入
- 部署脚本接入
- 前端 API 与页面接入

如果本次是落地 IoT 能力，推荐优先新增：
- `iot-device`
- `iot-collection`

并先完成第一阶段的管理、采集任务与查询能力，再逐步增强协议与实时处理能力。
