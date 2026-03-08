# Governance Frontend Monorepo

前端已整理为 `pnpm` monorepo，当前包含两个应用：

- `apps/govern`：后台管理系统
- `apps/portal`：门户端

整体按“应用层 / API 层 / 页面 UI 层 / 组件层 / 国际化层 / 基础工具层”组织。

## 目录结构

```text
web/
├─ apps/
│  ├─ govern/
│  └─ portal/
├─ packages/
│  ├─ api/
│  │  ├─ auth-center/
│  │  ├─ bms/
│  │  │  └─ user-management/
│  │  ├─ core/
│  │  ├─ data-source/
│  │  ├─ metadata-collection/
│  │  └─ workbench/
│  ├─ components/
│  │  ├─ common/
│  │  ├─ datasource/
│  │  ├─ governance-assistant/
│  │  └─ user/
│  ├─ i18n/
│  ├─ ui/
│  │  ├─ auth/
│  │  ├─ guards/
│  │  ├─ layouts/
│  │  └─ workbench/
│  └─ utils/
│     ├─ auth/
│     ├─ dictionary/
│     ├─ format/
│     └─ http/
├─ package.json
└─ pnpm-workspace.yaml
```

## 分层说明

### `apps/govern`

后台管理系统，只保留：

- 后台页面编排
- 后台路由注册
- 后台特有交互逻辑

当前已调整：

- 原 `apps/web` 已重命名为 `apps/govern`
- 顶部菜单已改为左侧导航
- 首页统计看板改为复用 `packages/ui/workbench`

### `apps/portal`

门户端应用，当前已落地：

- 登录页
- 首页统计看板

后续业务可以继续按 `features/*` 方式扩展。

### `packages/api`

公共业务接口层，按领域拆分：

- `auth-center`：登录、注册、当前用户、退出登录
- `bms/user-management`：用户、角色、权限管理
- `data-source`：数据源管理
- `metadata-collection`：元数据采集任务
- `workbench`：工作台概览
- `core`：公共响应类型

### `packages/ui`

页面级 UI 复用层，当前承载：

- `auth/`：认证页
- `guards/`：登录守卫、管理员守卫
- `layouts/`：`GovernanceAppShell`、`PortalAppShell`
- `workbench/`：门户与后台共用的首页统计面板

### `packages/components`

中小型业务组件层，适合在多个 `apps/*` 中复用。

当前已迁入：

- `governance-assistant/`：数据治理助手浮窗组件
- `common/EnabledTag`：启停状态标签
- `common/LanguageSelect`：语言切换器
- `datasource/DataSourceTypeTag`：数据源类型标签
- `user/RoleTag`：角色标签
- `user/UserStatusTag`：用户状态标签

### `packages/i18n`

国际化层，当前提供：

- 中文 / 英文双语字典
- 当前语言存储与切换
- `I18nProvider`
- Ant Design 语言包切换

### `packages/utils`

基础能力层，当前承载：

- `auth/`：token / 用户信息存储、路由常量、权限判断
- `dictionary/`：角色、数据源、元数据任务等字典映射与选项构造
- `format/`：日期格式化
- `http/`：统一请求封装、请求头注入、401 处理

当前已移除 `@governance/api -> @governance/utils -> @governance/api` 的循环依赖：

- `utils` 不再依赖 `api`
- 请求层保留在 `utils/http`
- 字典能力改为基于本地枚举值与 `i18n` 翻译

## 常用命令

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
