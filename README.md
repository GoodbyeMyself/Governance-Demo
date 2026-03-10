# Governance-Demo

数据治理演示项目，包含一套可本地运行、可单容器部署的微服务后端与前端 monorepo。

## 项目组成

- `microservice/`：Spring Boot + Spring Cloud Alibaba 微服务工程
- `web/`：基于 `pnpm` 的前端 monorepo，包含后台 `govern` 与门户 `portal`
- `DEPLOYMENT.md`：单容器 Docker 部署说明、访问地址与连接信息

## 当前整理结果

- 后端公共能力已收敛到 `microservice/common/service-support`
- 后端服务内包结构已按“职责优先”收缩，统一以 `com.governance.*` 为根包
- 后台应用已由 `apps/web` 重命名为 `apps/govern`
- 前端新增 `apps/portal` 门户应用，并补充 Demo 页面
- 前端请求封装、字典、布局、页面级 UI 与业务组件已拆分到公共包
- 国际化已支持 `zh-CN` / `en-US`，默认中文
- 国际化资源已按 `语言 -> shared/modules/apps` 分目录管理
- 登录/注册/找回密码流程已补齐验证码、记住密码、协议勾选与邮箱验证码能力
- 后台已新增个人资料编辑与角色定义管理页面
- 本地联调脚本已整理到 `microservice/scripts/local-dev`
- 单容器部署脚本已整理到 `microservice/scripts/docker-centos9`

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
│  └─ scripts/
│     ├─ local-dev
│     └─ docker-centos9
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
└─ DEPLOYMENT.md
```

## 快速开始

### 1. 构建后端

```powershell
cd microservice
mvn -DskipTests package
```

### 2. 构建前端

```powershell
cd web
pnpm install
pnpm build
```

### 3. 本地联调

后端本地联调脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\start-local.ps1
```

停止与健康检查：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\stop-local.ps1
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\local-dev\health-check.ps1
```

前端开发：

```powershell
cd web
pnpm dev:govern
pnpm dev:portal
```

## 单容器部署

一键初始化：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1
```

已构建产物场景可跳过构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1 -SkipBuild
```

默认部署后的本机访问地址：

- Govern：`http://127.0.0.1:19001`
- Portal：`http://127.0.0.1:19002`
- Swagger 聚合：`http://127.0.0.1:18080/swagger-ui.html`

## 文档入口

- 后端结构与运行说明：`microservice/README.md`
- 前端 monorepo 结构说明：`web/README.md`
- 部署与连接信息：`DEPLOYMENT.md`
- Govern 应用说明：`web/apps/govern/README.md`
- Portal 应用说明：`web/apps/portal/README.md`

## 默认账号

- 用户名：`admin`
- 密码：`Admin@123456`

## 当前认证流程说明

- 登录：用户名 + 密码 + 图形验证码 + 协议勾选
- 注册：用户名 + 密码 + 邮箱 + 图形验证码 + 邮箱验证码 + 协议勾选
- 找回密码：用户名 + 邮箱 + 新密码 + 图形验证码 + 邮箱验证码 + 协议勾选
- 个人资料：支持修改用户名、邮箱、手机号，后台会校验唯一性
- 后端接口已支持 `zh-CN` / `en-US` 多语言响应，前端会自动按当前语言透传 `Accept-Language`
- 单容器演示环境默认启用本地 SMTP 捕获服务，验证码邮件会落到容器目录 `/opt/governance-demo/mailbox`
- 代码更新后可直接执行增量发布脚本：`microservice/scripts/docker-centos9/update-single-container.ps1`
- 日志查看脚本：`microservice/scripts/docker-centos9/view-service-logs.ps1`
- 单服务启停脚本：`microservice/scripts/docker-centos9/manage-service.ps1`
