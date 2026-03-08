# Governance-Demo

数据治理演示项目，包含：

- `microservice/`：Spring Boot + Spring Cloud Alibaba 微服务后端
- `web/`：`pnpm` monorepo 前端
- `DEPLOYMENT.md`：CentOS 9 Docker 单容器部署说明

## 当前整理结果

- 后端新增 `microservice/common/service-support`，收敛重复的安全、异常、请求上下文和统一响应代码。
- `auth-center` 职责已收缩为认证中心，只保留登录、注册、当前用户和退出登录。
- 用户、角色、权限管理统一归口到 `bms-service`。
- 后端服务内包结构已压平，移除了多余的 `modules/*`、本地 `shared/*` 和单实现 `service/impl` 过渡层。
- 后端公共与业务根包已从 `com.governance.platform.*` 收缩为 `com.governance.*`。
- 前端后台应用已从 `apps/web` 重命名为 `apps/govern`。
- 前端新增 `apps/portal` 门户应用，并与后台共用首页统计面板。
- 前端新增 `packages/i18n`，已支持中文 / 英文切换，默认中文。
- 前端已拆除 `packages/api` 与 `packages/utils` 的循环依赖。

## 目录概览

```text
Governance-Demo/
├─ microservice/
│  ├─ common/service-support
│  ├─ gateway
│  ├─ service/
│  │  ├─ auth-center
│  │  ├─ bms-service
│  │  ├─ data-source
│  │  └─ data-metadata
│  ├─ nacos-config
│  └─ scripts
├─ web/
│  ├─ apps/
│  │  ├─ govern
│  │  └─ portal
│  └─ packages/
│     ├─ api
│     ├─ components
│     ├─ i18n
│     ├─ ui
│     └─ utils
├─ DEPLOYMENT.md
└─ DEPLOYMENT_SYSTEMD.md
```

## 文档入口

- 后端结构与本地联调：`microservice/README.md`
- 前端 monorepo 结构：`web/README.md`
- Docker 部署结果与连接信息：`DEPLOYMENT.md`

## 单容器一键部署

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1
```

如果前后端产物已经存在：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1 -SkipBuild
```

## 快速构建

后端：

```powershell
cd microservice
mvn -DskipTests package
```

前端：

```powershell
cd web
pnpm build
```

## 默认测试账号

- 用户名：`admin`
- 密码：`Admin@123456`
