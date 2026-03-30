# web

当前目录已开始按 monorepo 结构改造，现阶段已将后台应用下沉到 `apps/govern`，并约定后续新增：

- `apps/govern`：后台管理端（已完成 monorepo 接入）
- `apps/portal`：门户端（骨架已建立）
- `apps/screen`：大屏端（骨架已建立）

三端当前阶段统一保持 **Umi Max**，待整体结构稳定后再评估框架层调整。

## 当前开发入口

- 后台应用：`apps/govern`

Install `node_modules`:

```bash
pnpm install
```

## 常用命令

### 启动后台应用

```bash
pnpm dev:govern
```

### 启动门户应用

```bash
pnpm dev:portal
```

### 启动大屏应用

```bash
pnpm dev:screen
```

### 构建后台应用

```bash
pnpm build:govern
```

### 检查后台应用

```bash
pnpm lint:govern
```

### 测试后台应用

```bash
pnpm test:govern
```

## Provided Scripts

Govern provides some useful script to help you quick start and build with web project, code style check and test.

Scripts provided in `package.json`. It's safe to modify or add additional script:

### Start project

```bash
npm start
```

### Build project

```bash
npm run build
```

### Check code style

```bash
npm run lint
```

You can also use script to auto fix some lint error:

```bash
npm run lint:fix
```

### Test code

```bash
npm test
```
