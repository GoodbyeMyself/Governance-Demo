# web

当前 `web` 工程采用 `pnpm workspace` 管理前端 monorepo。

## 正式平台

- `apps/bms`: 基础平台
- `apps/govern`: 后台
- `apps/portal`: 门户
- `apps/screen`: 大屏

## 模板目录

- `templates/ant-design-pro-template`: Ant Design Pro 模板工程，不属于正式业务平台

## 共享包

- `packages/api`
- `packages/components`
- `packages/i18n`
- `packages/utils`

## 安装依赖

```bash
pnpm install
```

## 常用命令

```bash
pnpm dev:bms
pnpm dev:govern
pnpm dev:portal
pnpm dev:screen
```

```bash
pnpm build:bms
pnpm build:govern
pnpm build:portal
pnpm build:screen
```

```bash
pnpm lint
pnpm typecheck
pnpm test
```

## 说明

- `apps/*` 只保留正式业务平台
- 模板工程不再作为正式应用参与 workspace 治理
- 当前工程已完成 monorepo 结构接入，后续仍需继续收口共享边界和类型治理
