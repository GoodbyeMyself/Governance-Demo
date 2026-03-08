# Governance 微服务说明

## 1. 工程概览

当前后端采用 Spring Boot 多模块结构，重点目标是：

- 按服务边界拆分领域能力
- 把重复的安全、异常、请求透传能力收敛到公共层
- 避免服务内部继续出现“层级很多但目录很空”的结构
- 通过网关统一对外暴露接口与 Swagger 聚合入口

## 2. 顶层目录

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
   ├─ local-dev/
   └─ docker-centos9/
```

## 3. 模块职责

### `common/service-support`

公共支撑层，统一放置多个服务都要复用的基础能力：

- `shared/api`：统一响应模型
- `shared/config`：CORS 与请求链路相关配置
- `shared/exception`：公共异常与通用异常处理
- `shared/security`：网关透传鉴权、安全入口、访问拒绝处理
- `shared/web`：请求头透传与上下文保存

### `gateway`

网关是所有前端请求的统一入口，负责：

- JWT 鉴权与上下文解析
- 向下游服务透传用户、角色、请求 ID
- 对外统一暴露 `/auth`、`/bms`、`/source`、`/metadata` 等访问前缀
- 聚合 Swagger UI

### `service/auth-center`

认证中心，负责：

- 图形验证码生成
- 注册邮箱验证码发送
- 登录
- 注册
- 找回密码 / 重置密码
- 获取当前登录用户
- 更新当前用户资料
- 退出登录

说明：
- 认证中心接口已支持 `zh-CN` / `en-US` 多语言响应
- 单容器演示环境中的邮箱验证码通过本地 SMTP 捕获器发送，邮件文件位于 `/opt/governance-demo/mailbox`

- 该服务不再承担用户列表、角色修改等后台管理职责
- 用户主数据由 `bms-service` 维护，`auth-center` 通过内部接口调用

### `service/bms-service`

后台基础管理服务，负责：

- 用户注册与档案维护
- 用户资料唯一性校验（用户名 / 邮箱 / 手机号）
- 用户密码重置
- 用户角色更新
- 角色定义管理（`ADMIN` 只读）
- 角色、权限枚举输出
- 对 `auth-center` 提供内部用户查询与登录时间更新接口

### `service/data-source`

数据源管理服务，负责：

- 数据源新增、修改、删除、查询
- 输出数据源概览统计
- 对 `data-metadata` 提供内部数据源查询能力

### `service/data-metadata`

元数据服务，负责：

- 元数据采集任务管理
- 采集任务详情查询
- 工作台统计与趋势数据
- 对 `data-source` 提供内部引用计数能力

## 4. 服务内目录设计原则

当前每个微服务都遵循“够用即可”的目录组织方式，不再强行保留过深的空壳层级。

建议优先保留这些真实有内容的目录：

- `controller`：对外或对内接口
- `service`：领域服务接口与实现
- `repository`：持久化访问
- `entity`：实体与枚举
- `dto`：接口入参与出参
- `exception`：领域异常
- `config`：只保留当前服务真实需要的配置

统一约束：

- 根包统一使用 `com.governance`
- 如果某一层只有一个实现，也可以将接口与实现同目录保留，避免制造空层级
- 共享能力优先沉到 `common/service-support`，而不是在每个服务各复制一份

## 5. 本地联调脚本

本地联调脚本目录：

- `microservice/scripts/local-dev/start-local.ps1`
- `microservice/scripts/local-dev/stop-local.ps1`
- `microservice/scripts/local-dev/health-check.ps1`
- `microservice/scripts/local-dev/docker-compose.nacos.yml`

说明：

- `docker-compose.nacos.yml` 仅用于本地联调快速拉起 Nacos
- 本地联调默认依赖本机可访问的 MySQL
- 启动脚本会将 PID 写入 `microservice/logs/local-dev-pids.json`

## 6. 单容器部署脚本

单容器部署目录：

- `microservice/scripts/docker-centos9/init-single-container.ps1`
- `microservice/scripts/docker-centos9/bootstrap.sh`
- `microservice/scripts/docker-centos9/start-services.sh`
- `microservice/scripts/docker-centos9/stop-services.sh`
- `microservice/scripts/docker-centos9/nginx.governance-demo.conf`

## 7. 常用命令

### 构建全部后端模块

```powershell
cd microservice
mvn -DskipTests package
```

### 本地启动后端

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1
```

### 停止本地后端

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\stop-local.ps1
```

### 健康检查

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

## 8. Swagger 地址

### 本地联调模式

- 聚合 Swagger：`http://localhost:8080/swagger-ui.html`
- 认证服务文档：`http://localhost:8080/auth/v3/api-docs`
- 基础管理文档：`http://localhost:8080/bms/v3/api-docs`
- 数据源文档：`http://localhost:8080/source/v3/api-docs`
- 元数据文档：`http://localhost:8080/metadata/v3/api-docs`

### 单容器部署模式

- 聚合 Swagger：`http://127.0.0.1:18080/swagger-ui.html`
- 认证服务：`http://127.0.0.1:18081/swagger-ui.html`
- 基础管理：`http://127.0.0.1:18082/swagger-ui.html`
- 数据源：`http://127.0.0.1:18083/swagger-ui.html`
- 元数据：`http://127.0.0.1:18084/swagger-ui.html`

## 9. 运行顺序建议

本地联调建议顺序：

1. 启动 Nacos
2. 启动 MySQL
3. 启动 `bms-service`
4. 启动 `data-source`
5. 启动 `data-metadata`
6. 启动 `auth-center`
7. 启动 `gateway`
