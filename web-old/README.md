# Governance Web Monorepo

## 1. 工程概览

前端采用 `pnpm` monorepo，当前包含两个应用：

- `apps/govern`：后台管理端
- `apps/portal`：门户端

公共能力统一沉到 `packages/*`，避免各应用重复维护请求封装、布局、业务组件和国际化资源。

## 2. 当前目录结构

```text
web/
├─ apps/
│  ├─ govern/
│  └─ portal/
├─ packages/
│  ├─ api/
│  ├─ components/
│  ├─ i18n/
│  ├─ ui/
│  └─ utils/
├─ package.json
└─ pnpm-workspace.yaml
```

## 3. 分层说明

### `apps`

只放应用自身入口、路由注册和少量应用专属页面编排。

- `apps/govern`：后台管理端，左侧导航 + 顶部用户区
- `apps/portal`：门户端，顶部导航 + 首页 / Demo 页面

### `packages/api`

按领域拆分接口调用能力：

- `auth-center`
- `bms/role-management`
- `bms/user-management`
- `data-source`
- `metadata-collection`
- `workbench`
- `core`

### `packages/ui`

页面级复用 UI：

- `auth/`
- `guards/`
- `layouts/`
- `workbench/`

### `packages/components`

业务小组件与通用组件，适合在多个应用中复用：

- `common/`
- `datasource/`
- `governance-assistant/`
- `user/`

### `packages/utils`

基础能力与工具层：

- `auth/`：路由、token、用户信息、跨系统跳转地址构造
- `dictionary/`：枚举映射与下拉选项
- `format/`：格式化能力
- `http/`：统一请求封装

### `packages/i18n`

国际化资源按“语言 -> 分类 -> 文件”管理，避免所有词条堆在一个文件：

```text
packages/i18n/
├─ locales/
│  ├─ zh-CN/
│  │  ├─ shared/
│  │  ├─ modules/
│  │  └─ apps/
│  └─ en-US/
│     ├─ shared/
│     ├─ modules/
│     └─ apps/
├─ messages.ts
├─ react.tsx
├─ store.ts
└─ types.ts
```

说明：

- `shared/`：通用文案、导航、枚举、HTTP 提示
- `modules/`：按业务模块拆分，如 `data-source`、`data-metadata`
- `apps/`：按应用拆分，如 `govern`、`portal`
- 默认语言：`zh-CN`

## 4. 运行时配置

两个应用都支持通过 `public/runtime-config.js` 覆盖运行时配置：

- `assistant.title`
- `assistant.iframeUrl`
- `apps.governBaseUrl`
- `apps.portalBaseUrl`

用途：

- 配置数据治理助手地址
- 配置门户与后台之间的跨系统跳转地址

## 5. 常用命令

安装依赖：

```bash
pnpm install
```

启动后台：

```bash
pnpm dev:govern
```

启动门户：

```bash
pnpm dev:portal
```

构建全部前端：

```bash
pnpm build
```

仅构建后台：

```bash
pnpm build:govern
```

仅构建门户：

```bash
pnpm build:portal
```

## 6. 当前已落地能力

- `govern`：左侧导航、个人中心、数据源管理、元数据采集、用户管理、权限演示
- `govern`：左侧导航、个人中心资料编辑、角色定义管理、数据源管理、元数据采集、用户管理、权限演示
- `portal`：首页统计页、Demo 页面、顶部导航
- 两端右上角头像下拉均支持跨系统跳转
- 国际化支持中文 / 英文切换
- 登录页已支持验证码、记住密码、协议勾选、注册邮箱验证码与找回密码
