# shadcn-admin Monorepo 改造设计

## 1. 背景、边界与目标

当前仓库内存在一个独立的 `shadcn-admin` 前端工程，以及一个已采用 `pnpm workspace` 的 `web` 单仓工程。后续计划以 `shadcn-admin` 作为新的前端主工程，在其基础上逐步承接 `web` 中的业务逻辑。

本设计只定义 **第一阶段** 的范围。第一阶段的目标是：

- 以 `shadcn-admin` 作为新的前端主工程
- 将其改造为 `pnpm + monorepo` 结构
- 仅完成 monorepo 根骨架建立与 `apps/admin` 落位
- 为后续迁移 `web` 业务逻辑预留 `apps / packages` 承接位
- 保持现有 `shadcn-admin` 的 UI 效果、主题切换、布局切换与交互入口不变

### 1.1 第一阶段必须完成

第一阶段只包含以下内容：

1. 建立 monorepo 根骨架
2. 将当前 `shadcn-admin` 平移到 `apps/admin`
3. 打通 workspace 安装、开发与构建
4. 预留 `packages/*` 目录位或最小占位包

### 1.2 第一阶段明确不做

第一阶段 **不得** 包含以下内容：

- 不迁移 `web` 业务页面、业务路由、业务 API、认证流程实现
- 不抽离 `packages/ui`、`packages/shared`、`packages/config-*` 的实质代码
- 不重写现有页面
- 不切换 `shadcn-admin` 现有技术栈
- 不统一认证模型、路由模型或状态模型
- 不做超出 monorepo 工程改造范围的顺手优化

### 1.3 仓库边界说明

本设计讨论的是：**将当前 `shadcn-admin` 改造为新的前端 monorepo 主工程**。

本阶段不涉及：

- 将整个现有仓库统一改造成单一根 workspace
- 将现有 `web` 工程并入 `shadcn-admin`
- 修改 `web` 现有工程结构

在第一阶段完成前，`web` 保持原状，仅作为后续业务迁移的来源参考。

### 1.4 需求对齐表

| 已确认要求 | 本设计对应方式 | 是否纳入第一阶段 |
| --- | --- | --- |
| 以 `shadcn-admin` 为新前端主工程 | 以 `shadcn-admin` 为新 monorepo 根，并以 `apps/admin` 承载现有应用 | 是 |
| 第一阶段只做 `pnpm + monorepo` 工程改造 | 仅做根骨架、`apps/admin` 平移、workspace 打通、占位包预留 | 是 |
| 预留 `apps / packages` 承接位 | 创建 `packages/ui`、`packages/shared`、`packages/api`、`packages/business-*` 等占位结构 | 是 |
| 不迁移 `web` 业务逻辑 | 明确禁止迁入 `web` 页面、业务 API、业务路由、认证流程 | 是 |
| UI 效果保持不变，只重组工程结构 | 不主动改版现有视觉风格、布局结构与交互入口，仅允许最小必要路径调整 | 是 |

## 2. 现状分析

### 2.1 `shadcn-admin` 当前特征

`shadcn-admin` 当前是一个独立的 Vite 前端项目，具备以下关键特征：

- 使用 `Vite + React + TypeScript`
- 使用 `TanStack Router` 作为路由体系
- 使用 `Tailwind CSS v4 + shadcn/ui + Radix UI`
- 已内置完整后台 UI 壳、侧边栏、设置抽屉、命令面板、响应式布局
- 主题系统不仅包含深浅色切换，还包含布局模式、方向模式等外观控制能力

其中，以下能力是未来承接 `web` 迁移时最有价值的底座：

- `src/context/theme-provider.tsx` 提供的主题切换能力
- `src/components/config-drawer.tsx` 提供的外观配置能力
- `src/components/layout/authenticated-layout.tsx` 提供的后台布局壳
- `src/components/ui/*` 中沉淀的基础视觉组件与定制化 shadcn 组件

### 2.2 `web` 当前特征

现有 `web` 工程已经是 `pnpm workspace` 结构，但治理端业务仍以 `antd` 体系为主，当前 UI 壳与登录页逻辑分别集中在：

- `web/packages/ui/layouts/GovernanceAppShell.tsx`
- `web/packages/ui/auth/AuthCenterAuthPage.tsx`

这意味着后续迁移重点不是单纯搬运代码，而是要将原本围绕 Ant Design 组织的业务逻辑，逐步适配到 `shadcn-admin` 的视觉体系和应用结构中。

因此，第一阶段最重要的任务不是迁业务，而是先让 `shadcn-admin` 成为一个稳定、可扩展、可承接未来业务迁移的 monorepo 主工程。

## 3. 目标结构设计

### 3.1 第一阶段结构落位

第一阶段必须落位的结构如下：

```text
shadcn-admin/
├─ apps/
│  └─ admin/
├─ packages/
│  ├─ ui/
│  ├─ shared/
│  ├─ api/
│  ├─ business-auth/
│  ├─ business-system/
│  ├─ config-eslint/
│  ├─ config-typescript/
│  └─ config-prettier/
├─ package.json
├─ pnpm-workspace.yaml
├─ tsconfig.base.json
└─ ...
```

其中：

- `apps/admin` **必须** 承载当前 `shadcn-admin` 应用
- `packages/*` 在第一阶段 **允许为空目录、占位包或最小 package skeleton**
- 第一阶段 **不要求** `packages/ui`、`packages/shared`、`packages/config-*` 承载实际共享代码

### 3.2 未来职责规划

以下职责规划用于约束未来演进方向，**不作为第一阶段必须完成的代码拆分清单**。

#### `apps/admin`

作为当前 `shadcn-admin` 的主应用，负责：

- 应用启动入口
- 路由注册与路由生成接线
- QueryClient、RouterProvider、全局运行时 Provider 装配
- 页面装配与应用级错误页接线
- dev/build/preview 等应用级命令

该层保留应用运行时职责，不承载共享抽象。

#### `packages/ui`

作为未来前端体系的视觉底座，负责：

- `components/ui/*` 中的基础 UI 组件
- layout 相关基础组件
- theme/layout/direction/font 等偏 UI 的 provider
- 样式入口与主题变量
- `config-drawer`、`theme-switch`、导航进度、命令面板等增强型界面组件

该包的核心定位是“视觉资产包”，而不是整个前端内核包。

#### `packages/shared`

作为纯通用能力包，负责：

- 工具函数
- cookie 工具
- 无业务耦合的 hooks
- types、constants、helpers
- 与具体页面和具体应用运行时无关的共享逻辑

#### `packages/api`

后续用于承接：

- 请求客户端
- 认证接口
- 用户、角色、设备、数据源等业务 API 模块
- DTO / 响应类型定义

#### `packages/business-*`

未来建议按领域逐步承接旧 `web` 业务：

- `business-auth`
- `business-system`
- 后续可扩展 `business-device`、`business-datasource`、`business-metadata` 等

#### `packages/config-*`

未来负责沉淀：

- 公共 TypeScript 配置
- 公共 ESLint 配置
- 公共 Prettier 配置

## 4. 第一阶段迁移边界

### 4.1 第一阶段必须完成

#### A. 建立 monorepo 根骨架

必须完成：

- 根 `package.json`
- `pnpm-workspace.yaml`
- 根 `tsconfig.base.json`
- `apps/` 与 `packages/` 目录骨架

#### B. 平移当前应用至 `apps/admin`

必须完成：

- 当前应用入口文件迁入 `apps/admin`
- `src/**`、`public/**`、`index.html`、`vite.config.ts` 等应用内容平移
- 修正 workspace 下的脚本、路径、别名与构建入口

#### C. 打通 workspace 运行链路

必须完成：

- 依赖可在 workspace 下安装
- `apps/admin` 可开发启动
- `apps/admin` 可构建
- 路由生成、静态资源、样式入口可正常工作

#### D. 预留承接位

必须完成：

- `packages/ui`
- `packages/shared`
- `packages/api`
- `packages/business-auth`
- `packages/business-system`
- `packages/config-*`

以上在第一阶段只要求完成目录或最小占位，不要求承载实际共享实现。

### 4.2 第一阶段明确不做

第一阶段 **不得** 执行以下动作：

- 不抽 `packages/ui` 的实际组件、provider、样式代码
- 不抽 `packages/shared` 的实际工具代码
- 不抽 `packages/config-*` 的实际配置实现
- 不迁移 `web` 的业务页面、业务 hooks、业务 API、认证流程、治理端 shell
- 不主动调整现有页面视觉风格
- 不主动改动现有交互入口位置
- 不重构现有路由目录结构
- 不将页面级 feature 拆分到 `packages/business-*`

### 4.3 第一阶段保留在 `apps/admin` 的内容

以下内容在第一阶段应继续保留在 `apps/admin`：

- `routes/**`
- `routeTree.gen.ts`
- `features/**`
- QueryClient 初始化
- RouterProvider 装配
- devtools
- 应用级错误页接线
- 当前认证状态主流程

原因是这些内容仍然属于应用运行时与页面组织层，不属于第一阶段的工程骨架改造范围。

## 5. 方案比较与推荐

### 方案 A：以 `shadcn-admin` 为 monorepo 根，只做工程重组

做法：

- 保留当前技术栈不变
- 将当前工程平移为 `apps/admin`
- 预留 `packages/*` 承接位
- 暂不迁入 `web` 业务

优点：

- 最符合当前阶段目标
- UI 保真度最高
- 风险最低
- 后续可以自然承接业务迁移

缺点：

- 需要处理路径、别名、样式、Router 生成等工程性问题

### 方案 B：只做伪 monorepo 外壳

做法：

- 补齐 workspace 外壳，但不真正建立清晰的 `apps/admin` 与 `packages/*` 规划

优点：

- 改动最少

缺点：

- 后续还要再补结构边界，无法有效承接 `web` 迁移

### 方案 C：提前按未来平台做深度重构

做法：

- 第一阶段即重划分业务域、路由模型、主题模型与共享抽象

优点：

- 长期结构理想

缺点：

- 明显超出当前阶段范围
- 极易破坏现有 UI 效果
- 风险过高

### 推荐结论

推荐采用 **方案 A**，并且只采用其 **“骨架改造 + app 落位 + 占位包预留”** 子集。

也就是说，第一阶段不做共享代码抽离，只完成：

- monorepo 根骨架建立
- `apps/admin` 落位
- `packages/*` 预留
- workspace 开发与构建打通

## 6. 关键技术决策

### 6.1 保留现有技术栈

第一阶段不更改以下技术选型：

- Vite
- TanStack Router
- Tailwind CSS v4
- Zustand
- React Query

因为这些能力已与当前 `shadcn-admin` 的 UI 壳、主题体系与交互行为深度耦合。

### 6.2 `apps/admin` 负责应用编排

以下内容明确留在 app 层：

- 启动入口
- Router 初始化
- QueryClient 初始化
- devtools
- route tree 生成
- 应用级 not-found / error 组件接线

### 6.3 主题系统优先保真

以下能力在第一阶段只要求保留现状，不要求抽象或重组：

- `context/theme-provider.tsx`
- `components/config-drawer.tsx`
- `components/layout/authenticated-layout.tsx`

### 6.4 `packages/ui` 未来仅承担视觉资产职责

后续若进入共享代码抽离阶段，`packages/ui` 不应承载：

- 路由逻辑
- 页面级 feature
- QueryClient 初始化
- 应用级鉴权跳转与运行时编排

### 6.5 后续迁移 `web` 时按业务域承接

后续迁移时，不建议将旧 `web` 的结构原样照搬，而应按领域逐步落入新的 `packages/api` 与 `packages/business-*` 中。

## 7. 第一阶段实施顺序

### Phase 1：建立 monorepo 根骨架

输出：

- 根 `package.json`
- `pnpm-workspace.yaml`
- 根 `tsconfig.base.json`
- `apps/`、`packages/` 目录骨架

要求：

- workspace 能识别 `apps/*` 与 `packages/*`
- 除 `apps/admin` 外的其他 package 允许为空或仅占位

停止条件：

- 若 workspace 结构无法稳定识别 `apps/*` 与 `packages/*`，则停止进入下一阶段

### Phase 2：平移当前 app 至 `apps/admin`

输出：

- `apps/admin/package.json`
- `apps/admin/vite.config.ts`
- `apps/admin/index.html`
- `apps/admin/src/**`
- `apps/admin/public/**`

要求：

- `apps/admin` 能独立正常启动
- 页面、路由、主题、布局切换保持与现状一致
- 样式入口、静态资源、Router 生成正常

停止条件：

- 若 dev 失败、build 失败、路由不可访问、主题切换失效、布局切换失效、UI 明显偏离现状，则停止第一阶段，不进入任何后续共享代码抽离动作

## 8. 风险与控制措施

### 风险 1：TanStack Router 生成路径变化

问题：迁到 `apps/admin` 后，路由插件工作目录、生成文件位置、相对路径都可能失效。

控制措施：

- 第一阶段不改路由组织方式
- 仅修正插件根路径与生成位置
- 先保证 router 能正常生成与运行

停止 / 回滚条件：

- 若 Router 生成异常导致核心路由不可访问，则停止后续操作，仅保留已验证可运行的骨架与 app 平移结果

### 风险 2：Tailwind v4 样式入口与路径变化冲突

问题：平移至 `apps/admin`` 后，样式入口、CSS Variables 与静态资源路径可能失效。

控制措施：

- 先完成 app 平移并跑通
- 保持样式入口位置与加载方式尽量不变
- 不在第一阶段拆分样式到共享包

停止 / 回滚条件：

- 若主题、布局或页面样式与现状出现明显差异，则停止继续改造，仅修复到 UI 恢复一致为止

### 风险 3：`@/` 别名在新目录下失效

问题：当前代码大量依赖 `@/` 指向 `src`，平移后若 alias 未修复，会导致构建与运行失败。

控制措施：

- `@/` 继续只指向 `apps/admin/src`
- 第一阶段不引入共享包别名重构

停止 / 回滚条件：

- 若 alias 修复后仍引发大面积 import 改写需求，则停止扩大范围，不进入共享代码抽离

### 风险 4：UI 组件与 Provider 隐式耦合

问题：部分 UI 能力依赖 `theme/layout/sidebar/direction/search` 等上下文。

控制措施：

- 第一阶段不拆这部分代码
- 保持其继续留在 `apps/admin` 内部

停止 / 回滚条件：

- 若出现必须重构 provider 关系才能运行的情况，则说明已超出第一阶段边界，应停止并回退到仅骨架改造

### 风险 5：后续旧 `web` 迁移与新视觉体系冲突

问题：旧 `web` 目前仍以 Ant Design 为主，迁入时会与新的视觉和结构体系产生明显冲突。

控制措施：

- 第一阶段不处理这个冲突
- 本阶段仅完成可承接的工程底座搭建
- 第二阶段再按领域逐步迁移业务

## 9. 第一阶段验收标准

本阶段完成后，应满足以下条件。

### 9.1 工程结构验收

- `shadcn-admin` 已成为新的 monorepo 根
- `apps/admin` 已承载当前主应用
- `packages/ui`、`packages/shared`、`packages/api`、`packages/business-*`、`packages/config-*` 已完成目录或占位包预留
- 根级 workspace 配置可识别 `apps/*` 与 `packages/*`

### 9.2 运行验收

- `apps/admin` 开发启动正常
- `apps/admin` 构建正常
- 路由生成正常
- 静态资源访问正常
- 样式入口加载正常

### 9.3 UI 保真验收

- 已有页面路由可正常打开
- 明暗主题切换可用
- 布局模式切换可用
- 方向模式切换可用
- 侧边栏、设置抽屉、命令面板等交互可用
- 未主动变更现有 UI 视觉风格、布局结构与交互入口位置

### 9.4 范围约束验收

- 未迁入 `web` 的业务页面
- 未迁入 `web` 的业务路由
- 未迁入 `web` 的业务 API 与认证流程实现
- 未将页面级 feature 拆入 `packages/business-*`
- `packages/*` 仅为承接位或占位包，不要求承载实际共享实现

## 10. 后续阶段候选工作（不属于第一阶段）

以下内容不属于第一阶段，仅作为后续阶段建议：

1. 抽离公共配置到 `packages/config-*`
2. 抽离通用工具到 `packages/shared`
3. 抽离视觉资产到 `packages/ui`
4. 建立 `packages/api` 的请求层与接口模块
5. 按业务域逐步承接旧 `web` 逻辑

后续迁移建议顺序：

1. 共享常量 / types / utils
2. API 封装层
3. 登录与认证页面
4. 治理端 shell（导航、header、用户菜单、语言切换）
5. 用户 / 角色 / 权限类模块
6. 设备 / 数据源 / 元数据类模块
7. 跨应用联动与上下文统一

## 11. 结论

本设计建议以 `shadcn-admin` 为新的前端主工程，第一阶段仅完成 `pnpm + monorepo` 工程改造，确保当前应用以尽量原样的方式稳定落位到 `apps/admin` 中。

第一阶段的核心不是共享代码抽离，更不是业务迁移，而是：

- 建立 monorepo 根骨架
- 平移当前应用
- 打通 workspace 开发与构建
- 预留未来承接 `web` 迁移的标准包位

在此基础稳定后，再进入后续阶段，逐步抽离共享能力并迁移旧 `web` 业务逻辑。