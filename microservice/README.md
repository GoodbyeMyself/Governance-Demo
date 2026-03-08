# Governance 微服务说明

## 1. 优化后的目录结构

```text
microservice/
├─ pom.xml
├─ common/
│  └─ service-support/
├─ gateway/
├─ service/
│  ├─ auth-center/
│  ├─ bms-service/
│  ├─ data-source/
│  └─ data-metadata/
├─ nacos-config/
└─ scripts/
```

## 2. 目录职责

### `common/service-support`

公共支撑层，负责收敛多个服务重复实现：

- `shared/api`：统一响应模型 `ApiResponse`
- `shared/config`：CORS 与请求拦截器配置
- `shared/exception`：公共异常基类与通用异常
- `shared/security`：网关透传鉴权、统一 Spring Security 配置
- `shared/web`：请求头透传与上下文保存

这层的目标是让各业务服务只保留领域代码，避免在每个服务内复制一套 `shared/*`。

### `gateway`

统一 API 入口，负责：

- 基于 Nacos 的服务发现与路由转发
- 认证上下文透传
- 对外暴露统一访问地址 `/api/**`

### `service/auth-center`

认证中心，当前只负责：

- `POST /api/auth-center/register`
- `POST /api/auth-center/login`
- `GET /api/auth-center/me`
- `POST /api/auth-center/logout`

已去掉的内容：

- 本地用户表残留实体 / 仓库 / JPA 依赖
- 用户列表与角色修改代理接口

### `service/bms-service`

基础管理服务，负责：

- 用户管理
- 角色管理
- 权限管理
- 供 `auth-center` 调用的内部用户接口

对外用户管理接口：

- `GET /api/bms/users`
- `PUT /api/bms/users/{id}/role`
- `GET /api/bms/roles`
- `GET /api/bms/permissions`

### `service/data-source`

数据源管理服务，负责数据源 CRUD 以及对元数据服务提供内部查询能力。

### `service/data-metadata`

元数据采集与工作台服务，负责采集任务、任务明细、工作台统计等能力。

## 3. 服务内源码包规范

为避免单个微服务内部继续出现过深的“空壳层级”，当前各服务统一按“服务根包 + 领域包”组织源码：

- 不再保留仅起中转作用的 `modules/*` 包层级
- 服务私有配置不再放在本地 `shared/*` 下，统一收口到服务根包的 `config`、`exception`、`security`
- 只有一个实现类的场景，`service/impl` 会合并回 `service`
- 对外部服务的 Feign 调用统一放到 `integration/<target>`，不再额外套一层 `client`

以 `data-source` 为例，当前推荐结构如下：

```text
com.governance
├─ datasource
│  ├─ controller
│  ├─ dto
│  ├─ entity
│  ├─ exception
│  ├─ integration/metadata
│  ├─ repository
│  └─ service
├─ config
└─ exception
```

其他服务对应关系如下：

- `auth-center`：`authcenter/*`
- `bms-service`：`bms/user/*`
- `data-metadata`：`metadata/*`、`workbench/*`

## 4. 当前结构调整结论

### 已修正的问题

- 多个服务内重复的 `shared` 代码已抽到 `common/service-support`
- `auth-center` 与 `bms-service` 的职责边界已分清
- Nacos 配置已改为环境变量可覆盖，便于 Docker / 本地双场景复用
- 统一异常处理已限制在业务控制器范围内，避免误作用于框架端点
- 服务内部包层级已压平，去掉了多余的 `modules/*` 与本地 `shared/*` 过渡目录

### 当前推荐分层

- `common/*`：跨服务公共能力
- `gateway/`：接入层
- `service/*`：业务服务层
- `nacos-config/`：运行期配置中心内容
- `scripts/`：本地开发脚本

这个层级对当前规模是合理的，不建议再额外拆更细的“基础设施服务层目录”，否则会增加迁移成本而收益有限。

## 5. 配置说明

### Nacos

版本化配置位于 `microservice/nacos-config`。

当前已支持通过环境变量覆盖：

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `NACOS_SERVER_ADDR`
- `GATEWAY_TRUSTED_TOKEN`
- `AUTH_CENTER_JWT_SECRET`
- `AUTH_CENTER_JWT_EXPIRE_SECONDS`

### 数据库

当前服务使用的库：

- `bms_service_db`
- `data_source_db`
- `data_metadata_db`

`auth-center` 不再持有独立业务库。

## 6. 本地构建

```powershell
cd microservice
mvn -DskipTests package
```

## 7. 本地运行建议

如果只做后端联调，推荐顺序：

1. 启动 Nacos
2. 启动 MySQL
3. 启动 `bms-service`
4. 启动 `data-source`
5. 启动 `data-metadata`
6. 启动 `auth-center`
7. 启动 `gateway`

项目内脚本已按用途拆分目录：

- 本地联调脚本目录：`microservice/scripts/local-dev`
- 单容器部署脚本目录：`microservice/scripts/docker-centos9`

本地联调常用脚本：

- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/stop-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`
- `microservice/scripts/local-dev/docker-compose.nacos.yml`（仅用于本地联调启动 Nacos）

## 8. Swagger 入口

推荐优先使用网关聚合入口：

- 聚合 Swagger：`http://localhost:8080/swagger-ui.html`
- 认证服务 OpenAPI：`http://localhost:8080/auth/v3/api-docs`
- 基础管理服务 OpenAPI：`http://localhost:8080/bms/v3/api-docs`
- 数据源服务 OpenAPI：`http://localhost:8080/source/v3/api-docs`
- 元数据服务 OpenAPI：`http://localhost:8080/metadata/v3/api-docs`

如果需要直连单个服务的 Swagger UI：

- `auth-center`：`http://localhost:8081/swagger-ui.html`
- `bms-service`：`http://localhost:8082/swagger-ui.html`
- `data-source`：`http://localhost:8083/swagger-ui.html`
- `data-metadata`：`http://localhost:8084/swagger-ui.html`

## 9. Docker CentOS9 脚本

仓库内已补充与当前容器一致的部署脚本模板，位置如下：

- `microservice/scripts/docker-centos9/init-single-container.ps1`
- `microservice/scripts/docker-centos9/bootstrap.sh`
- `microservice/scripts/docker-centos9/start-services.sh`
- `microservice/scripts/docker-centos9/stop-services.sh`
- `microservice/scripts/docker-centos9/runtime.env.example`
- `microservice/scripts/docker-centos9/nginx.governance-demo.conf`

推荐优先使用宿主机一键脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1
```

如果前后端产物已经提前构建完成，可以直接执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1 -SkipBuild
```

该脚本会在宿主机侧完成以下动作：

- 创建并启动单个 `governance-centos9` 容器
- 在容器内安装 `mysql-server`、`nginx`、`openssh-server`、`java-17`
- 复制 Nacos 运行目录、前端静态产物、后端 JAR、Nacos 配置和启动脚本
- 初始化数据库、导入运行时环境变量并拉起全部服务
- 自动验证 SSH、Nacos、网关、前端、Swagger 和登录链路

这些脚本与当前已部署容器 `governance-centos9` 内使用的脚本保持一致，已可直接用于后续复现部署。
