# Governance Demo 本地开发调试说明

本文档用于说明 `Governance-Demo` 在本地开发环境下的启动、联调、排障与常用命令。

适用范围：

- 后端：`microservice/`
- 前端后台：`web/apps/govern`
- 前端门户：`web/apps/portal`
- 前端共享包：`web/packages/*`

与 [DEPLOYMENT.md](/E:/Governance-Demo/docs/DEPLOYMENT.md) 的区别：

- `DEPLOYMENT.md` 面向单容器部署与演示环境
- 本文档面向开发机上的本地启动、IDEA 调试、接口联调与问题排查

## 1. 本地开发模式

当前项目推荐采用如下本地开发模式：

- MySQL：本机安装，或连接外部可达实例
- Nacos：通过 Docker 本地拉起
- 后端微服务：本机直接运行
- 前端应用：通过 `pnpm` 本地运行

后端本地联调脚本位于：

- [start-local.ps1](/E:/Governance-Demo/microservice/scripts/local-dev/start-local.ps1)
- [stop-local.ps1](/E:/Governance-Demo/microservice/scripts/local-dev/stop-local.ps1)
- [health-check.ps1](/E:/Governance-Demo/microservice/scripts/local-dev/health-check.ps1)
- [docker-compose.nacos.yml](/E:/Governance-Demo/microservice/scripts/local-dev/docker-compose.nacos.yml)

## 2. 环境准备

建议本地环境至少具备以下依赖：

- JDK 17
- Maven 3.9+
- Node.js 20+
- `pnpm`
- Docker Desktop
- MySQL 8.x
- IntelliJ IDEA

建议额外确认：

- 本机 `3306` 端口可用于 MySQL，或已配置外部数据库地址
- 本机 `8848`、`9848` 端口可用于 Nacos
- 本机 `8080` 至 `8086` 端口未被其他服务长期占用

## 3. 后端本地调试

### 3.1 启动前提

本地联调脚本默认假设：

- Nacos 使用 `127.0.0.1:8848`
- Nacos gRPC 使用 `127.0.0.1:9848`
- MySQL 使用 `127.0.0.1:3306`

如果未显式指定环境变量，`data-metadata` 等服务会默认连接本地 MySQL。以元数据服务为例，默认配置见：

- [data-metadata application.yml](/E:/Governance-Demo/microservice/service/data-metadata/src/main/resources/application.yml)

如果你不使用本地 MySQL，可以在启动前设置：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

### 3.2 一键启动后端

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1
```

脚本会依次完成以下动作：

- 检查并确保 Nacos 可用
- 必要时通过 Docker 拉起本地 Nacos
- 检查 MySQL 连通性
- 将 `common/service-support` 安装到本地 Maven 仓库
- 依次启动所有后端服务
- 对每个服务执行健康检查

默认启动的服务如下：

- `gateway`
- `auth-center`
- `bms-service`
- `data-source`
- `data-metadata`
- `iot-device`
- `iot-collection`

### 3.3 常用启动参数

```powershell
# 启动前先重新构建全部模块
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1 -Build

# 强制通过 Docker 启动 Nacos
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1 -StartNacos

# 跳过基础设施检查
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1 -SkipInfraCheck

# 自定义健康检查等待时长
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1 -TimeoutSeconds 240
```

### 3.4 停止后端

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\stop-local.ps1
```

如需强制停止：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\stop-local.ps1 -Force
```

### 3.5 健康检查

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

说明：

- 返回 `UP` 表示服务正常
- 返回 `UP(AUTH)` 通常表示服务可达，只是健康检查接口被鉴权拦截
- 返回 `DOWN` 表示服务未启动或不可达

### 3.6 本地日志与 PID 文件

本地联调脚本默认输出到：

- 日志目录：`microservice/logs`
- PID 文件：`microservice/logs/local-dev-pids.json`

常见日志文件：

- `microservice/logs/gateway.out.log`
- `microservice/logs/auth-center.out.log`
- `microservice/logs/bms-service.out.log`
- `microservice/logs/data-source.out.log`
- `microservice/logs/data-metadata.out.log`
- `microservice/logs/iot-device.out.log`
- `microservice/logs/iot-collection.out.log`

## 4. 使用 IDEA 单独调试后端服务

如果需要单独断点调试某个服务，推荐直接在 IDEA 中运行对应的 Spring Boot 启动类。

当前主要启动类：

- `com.governance.GovernanceAuthServiceApplication`
- `com.governance.BmsServiceApplication`
- `com.governance.DataSourceApplication`
- `com.governance.DataMetadataApplication`
- `com.governance.IotDeviceApplication`
- `com.governance.IotCollectionApplication`
- `com.governance.GovernanceGatewayApplication`

推荐顺序：

1. 先保证 MySQL 与 Nacos 正常
2. 再启动业务服务
3. 最后启动 `gateway`

如果某个服务在 IDEA 中显示运行入口发灰，优先检查：

- 根 [pom.xml](/E:/Governance-Demo/microservice/pom.xml) 是否已经纳入该模块
- IDEA 是否已执行 Maven Reload
- 对应模块是否被正确识别为 Maven 子模块

## 5. 后端本地端口

### 5.1 微服务端口

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| `gateway` | `8080` | 网关统一入口 |
| `auth-center` | `8081` | 认证中心 |
| `bms-service` | `8082` | 后台基础管理 |
| `data-source` | `8083` | 数据源服务 |
| `data-metadata` | `8084` | 元数据服务 |
| `iot-device` | `8085` | IoT 设备服务 |
| `iot-collection` | `8086` | IoT 采集服务 |

### 5.2 基础设施端口

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | `3306` | 本地默认数据库端口 |
| Nacos HTTP | `8848` | 配置中心与注册中心 HTTP 端口 |
| Nacos gRPC | `9848` | Nacos gRPC 端口 |

## 6. 本地访问地址

### 6.1 网关与后端

- Gateway 健康检查：`http://127.0.0.1:8080/actuator/health`
- Swagger 聚合入口：`http://127.0.0.1:8080/swagger-ui.html`
- 认证中心 OpenAPI：`http://127.0.0.1:8080/auth/v3/api-docs`
- BMS OpenAPI：`http://127.0.0.1:8080/bms/v3/api-docs`
- 数据源 OpenAPI：`http://127.0.0.1:8080/source/v3/api-docs`
- 元数据 OpenAPI：`http://127.0.0.1:8080/metadata/v3/api-docs`

### 6.2 单服务 Swagger

- 认证中心：`http://127.0.0.1:8081/swagger-ui.html`
- 后台管理：`http://127.0.0.1:8082/swagger-ui.html`
- 数据源服务：`http://127.0.0.1:8083/swagger-ui.html`
- 元数据服务：`http://127.0.0.1:8084/swagger-ui.html`

### 6.3 Nacos

- Nacos 控制台：`http://127.0.0.1:8848/nacos`

## 7. 前端本地开发

前端工程使用 `pnpm workspace` 管理，说明见：

- [web/README.md](/E:/Governance-Demo/web/README.md)

### 7.1 安装依赖

```powershell
cd .\web
pnpm install
```

### 7.2 启动 Govern

```powershell
cd .\web
pnpm dev:govern
```

### 7.3 启动 Portal

```powershell
cd .\web
pnpm dev:portal
```

说明：

- `Umi Max` 开发服务默认通常监听 `8000`
- 如果端口已被占用，开发服务器可能自动顺延到其他端口
- 实际访问地址以终端输出为准

### 7.4 前端联调注意事项

当前 `govern` 的代理配置仍然保留了模板默认目标，见：

- [govern proxy.ts](/E:/Governance-Demo/web/apps/govern/config/proxy.ts)

这意味着：

- 如果不调整代理，`govern` 本地开发环境未必会自动请求到本地 `gateway`
- 进行本地接口联调前，建议先将前端 `/api/` 代理目标改为本地网关，例如 `http://127.0.0.1:8080`

如果你要同时联调 `govern` 登录与后端认证中心，这一步是必须确认的。

## 8. 常用本地联调命令

### 8.1 后端

```powershell
# 一键启动本地后端
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1

# 一键停止本地后端
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\stop-local.ps1

# 查看后端健康状态
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

### 8.2 前端

```powershell
cd .\web
pnpm dev:govern
pnpm dev:portal
```

### 8.3 全量构建

```powershell
cd .\microservice
mvn -DskipTests clean package

cd ..\web
pnpm build
```

## 9. 推荐联调顺序

推荐按以下顺序进行本地联调：

1. 先确认 MySQL 可用
2. 再启动 Nacos
3. 执行后端本地联调脚本
4. 运行健康检查脚本
5. 启动 `govern` 或 `portal`
6. 确认前端代理已指向本地 Gateway
7. 再开始页面与接口联调

## 10. 常见问题排查

### 10.1 Nacos 没有起来

优先检查：

- Docker Desktop 是否已启动
- `8848`、`9848` 是否被占用
- 执行：

```powershell
docker compose -f .\microservice\scripts\local-dev\docker-compose.nacos.yml up -d
```

### 10.2 MySQL 3306 不可达

启动脚本会在默认使用本地数据库时检查 `3306` 端口。

如果本机没有 MySQL，可以：

- 启动本地 MySQL
- 或在启动前设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`

### 10.3 某个服务没有起来

先看：

- `microservice/logs/<service>.out.log`
- `microservice/logs/<service>.err.log`

再执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

### 10.4 `data-metadata` 在 IDEA 中启动入口发灰

优先检查：

- 根 [pom.xml](/E:/Governance-Demo/microservice/pom.xml) 是否纳入 `service/data-metadata`
- IDEA 是否执行过 Maven Reload
- 运行配置是否指向 `com.governance.DataMetadataApplication`

### 10.5 前端页面能打开但接口没走本地

优先检查：

- `govern` 或 `portal` 当前代理目标
- 浏览器开发者工具中的请求地址
- 是否实际请求到了 `http://127.0.0.1:8080/api/...`

## 11. 当前本地开发限制说明

- 本文档描述的是开发态运行方式，不等价于生产或演示部署环境
- 当前前端代理配置仍需要根据联调目标切换到本地 Gateway
- 某些前端默认端口未在配置文件中固定写死，需以启动终端输出为准
- 若使用 IDEA 单独启动服务，仍需自行保证 Nacos、MySQL 与依赖服务已准备完成
