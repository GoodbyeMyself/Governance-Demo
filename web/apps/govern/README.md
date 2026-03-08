# Govern App

`apps/govern` 是后台管理端应用，对应数据治理后台。

## 当前职责

- 后台路由注册
- 后台页面编排
- 与 `packages/ui/layouts/GovernanceAppShell` 配合完成整体壳布局

## 当前结构

```text
src/
├─ features/
│  ├─ access/
│  ├─ auth/
│  ├─ data-source/
│  ├─ home/
│  ├─ metadata-collection/
│  ├─ profile/
│  ├─ role-management/
│  └─ user-management/
├─ App.tsx
├─ global.less
└─ main.tsx
```

## 页面特点

- 顶部菜单已收敛为左侧导航
- 右上角头像下拉包含：
  - 个人中心
  - 跳转门户
  - 退出登录
- 登录页采用左右分栏，支持验证码、记住密码、注册、找回密码
- 个人中心支持编辑用户名、邮箱、手机号
- 角色定义管理页面支持查看 / 编辑角色定义，其中 `ADMIN` 只读
- 页面右下角集成数据治理助手浮窗

## 开发

```bash
pnpm dev
```

## 构建

```bash
pnpm build
```
