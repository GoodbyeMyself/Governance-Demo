# Governance Demo 单容器部署说明

本文档对应当前最新结构：

- 后端：`microservice/`
- 前端后台：`web/apps/govern`
- 前端门户：`web/apps/portal`
- 前端公共层：`web/packages/*`

## 1. 部署方式

当前采用 **单 CentOS 9 容器部署**，容器名：

- `governance-centos9`

容器内统一承载：

- `sshd`
- `mysqld`
- `nacos`
- `nginx`
- `gateway`
- `auth-center`
- `bms-service`
- `data-source`
- `data-metadata`

## 2. 一键初始化命令

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1
```

如果后端和前端产物已经构建完成，可以跳过构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\microservice\scripts\docker-centos9\init-single-container.ps1 -SkipBuild
```

## 3. 当前容器内目录

项目基础目录：

- `/opt/governance-demo`

其中关键路径：

- 后台前端产物：`/opt/governance-demo/govern`
- 门户前端产物：`/opt/governance-demo/portal`
- 后端 Jar：`/opt/governance-demo/jars`
- Nacos 配置：`/opt/governance-demo/nacos-config`
- 运行脚本：`/opt/governance-demo/run`
- 日志目录：`/opt/governance-demo/logs`

## 4. 访问地址

### 前端

- 后台管理系统 Govern：`http://127.0.0.1:19001`
- 门户 Portal：`http://127.0.0.1:19002`
- Govern 登录页已支持：图形验证码、记住密码、注册、找回密码、协议勾选

### 后端与中间件

- Gateway：`http://127.0.0.1:18080`
- Nacos：`http://127.0.0.1:18848/nacos`
- 聚合 Swagger：`http://127.0.0.1:18080/swagger-ui.html`

### Swagger 文档地址

- 聚合入口：`http://127.0.0.1:18080/swagger-ui.html`
- 认证中心 UI：`http://127.0.0.1:18081/swagger-ui.html`
- 后台管理 UI：`http://127.0.0.1:18082/swagger-ui.html`
- 数据源服务 UI：`http://127.0.0.1:18083/swagger-ui.html`
- 元数据服务 UI：`http://127.0.0.1:18084/swagger-ui.html`
- 认证中心：`http://127.0.0.1:18080/auth/v3/api-docs`
- 后台管理：`http://127.0.0.1:18080/bms/v3/api-docs`
- 数据源服务：`http://127.0.0.1:18080/source/v3/api-docs`
- 元数据服务：`http://127.0.0.1:18080/metadata/v3/api-docs`

## 5. 账号与连接信息

### SSH

- Host：`127.0.0.1`
- Port：`12222`
- Username：`root`
- Password：`Governance@2026SSH`

### MySQL

- Host：`127.0.0.1`
- Port：`23306`
- Username：`governance`
- Password：`Governance@2026`

### 默认后台账号

- Username：`admin`
- Password：`Admin@123456`

### 邮箱验证码模式

- 当前单容器演示环境默认启用真实 SMTP 发送链路，但 SMTP 服务使用容器内本地捕获器承接
- 发送邮箱验证码接口：`POST http://127.0.0.1:18080/api/auth-center/email-codes/send`
- 当前环境不会再通过接口返回 `data.debugCode`
- 认证中心当前邮件环境变量：
  - `AUTH_CENTER_MAIL_MOCK_ENABLED=false`
  - `MAIL_HOST=127.0.0.1`
  - `MAIL_PORT=1025`
  - `AUTH_CENTER_MAIL_FROM=no-reply@governance.local`
- 邮件原文存放目录：`/opt/governance-demo/mailbox`
- 如需切换到真实外部 SMTP，只需把 `MAIL_HOST`、`MAIL_PORT`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`MAIL_SMTP_AUTH`、`MAIL_SMTP_STARTTLS_ENABLE` 改成目标邮件服务配置
- 后端接口已支持 `zh-CN` / `en-US` 多语言响应，前端会自动按当前语言透传 `Accept-Language`

## 6. 容器外如何连接并进入 Shell

### 方式一：使用系统自带 `ssh`

```powershell
ssh root@127.0.0.1 -p 12222
```

首次连接输入 `yes`，然后输入密码：

- `Governance@2026SSH`

### 方式二：使用 PowerShell 直接执行远程命令

```powershell
ssh root@127.0.0.1 -p 12222 "hostname && ps -ef | grep java"
```

### 方式三：本机直接进入 Docker 容器

如果只是当前电脑本机临时进入容器，也可以直接执行：

```powershell
docker exec -it governance-centos9 bash
```

## 7. 外部数据库连接示例

可使用 Navicat、DBeaver 或命令行直接连接：

```bash
mysql -h127.0.0.1 -P23306 -ugovernance -pGovernance@2026
```

## 8. 常用启停命令

### 启动容器

```powershell
docker start governance-centos9
```

### 容器启动后，一键拉起内部服务

```powershell
docker exec governance-centos9 bash -lc "/opt/governance-demo/run/bootstrap.sh"
```

### 停止微服务

```powershell
docker exec governance-centos9 bash -lc "/opt/governance-demo/run/stop-services.sh"
```

### 停止容器

```powershell
docker stop governance-centos9
```

## 9. 当前端口映射

```text
12222 -> 22     sshd
19001 -> 9001   nginx / govern
19002 -> 9002   nginx / portal
18080 -> 8080   gateway
18081 -> 8081   auth-center
18082 -> 8082   bms-service
18083 -> 8083   data-source
18084 -> 8084   data-metadata
18848 -> 8848   nacos http
19848 -> 9848   nacos grpc
19849 -> 9849   nacos grpc raft
23306 -> 3306   mysql
```

## 10. 已执行验证

### 前端

- `http://127.0.0.1:19001` 返回 `200`
- `http://127.0.0.1:19002` 返回 `200`

### Nacos

- `http://127.0.0.1:18848/nacos` 返回 `200`

### SSH

- `127.0.0.1:12222` 端口连通
- 已使用 `root / Governance@2026SSH` 完成密码登录验证

### 网关与认证

- `GET http://127.0.0.1:18080/actuator/health` 返回 `200`
- `GET http://127.0.0.1:18080/api/auth-center/captcha` 返回 `200`
- 认证中心公开接口已通过网关白名单放行：
  - `/api/auth-center/captcha`
  - `/api/auth-center/login`
  - `/api/auth-center/register`
  - `/api/auth-center/email-codes/send`
  - `/api/auth-center/password/reset`

### Swagger

- `http://127.0.0.1:18080/swagger-ui.html` 返回 `200`
- `http://127.0.0.1:18080/auth/v3/api-docs` 返回 `200`
- `http://127.0.0.1:18080/bms/v3/api-docs` 返回 `200`
- `http://127.0.0.1:18080/source/v3/api-docs` 返回 `200`
- `http://127.0.0.1:18080/metadata/v3/api-docs` 返回 `200`
- `http://127.0.0.1:18081/swagger-ui.html` 返回 `200`
- `http://127.0.0.1:18082/swagger-ui.html` 返回 `200`
- `http://127.0.0.1:18083/swagger-ui.html` 返回 `200`
- `http://127.0.0.1:18084/swagger-ui.html` 返回 `200`

### 数据库外部访问

- 已从容器外通过 `127.0.0.1:23306` 连接 MySQL

## 11. 本次部署对应的代码结构说明

本次部署基于以下结构调整后的代码：

- 后端公共能力抽到 `microservice/common/service-support`
- `auth-center` 已补齐验证码、邮箱验证码、注册、登录、找回密码、资料修改职责
- `bms-service` 已补齐资料唯一性校验、密码重置与角色定义管理
- 后台前端由 `apps/web` 调整为 `apps/govern`
- 新增 `apps/portal` 门户应用
- 前端新增 `packages/i18n`，支持中英文切换
- 后台首页与门户首页统计面板统一复用 `packages/ui/workbench`
- 已拆除前端 `packages/api` 与 `packages/utils` 的循环依赖
- 前端已补齐登录/注册/找回密码流转、记住密码、协议勾选、个人资料编辑与角色定义页面
