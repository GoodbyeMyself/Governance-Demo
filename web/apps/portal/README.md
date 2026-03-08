# Portal App

`apps/portal` 是门户端应用，对应对外展示型的数据治理门户。

## 当前职责

- 门户首页与演示页路由注册
- 门户页面编排
- 与 `packages/ui/layouts/PortalAppShell` 配合完成顶部导航布局

## 当前页面

- 登录页
- 门户首页
- 门户 Demo 页面

## 页面特点

- 顶部常驻导航包含：
  - 首页
  - Demo
- 右上角头像下拉包含：
  - 跳转后台
  - 退出登录
- 登录页复用统一认证中心页面，支持验证码、注册、找回密码与协议勾选

## 开发

```bash
pnpm dev
```

## 构建

```bash
pnpm build
```
