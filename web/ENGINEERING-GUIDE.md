# 工程说明与开发约束

## 1. 当前工程状态

`web` 当前已经完成 monorepo 主体收口，可以正式进入业务开发阶段。

当前正式应用为四端：

- `apps/bms`
- `apps/govern`
- `apps/portal`
- `apps/screen`

当前共享包为：

- `packages/api`
- `packages/components`
- `packages/i18n`
- `packages/mocks`
- `packages/utils`

当前工作区定义见 [pnpm-workspace.yaml](E:\Governance-Demo\web\pnpm-workspace.yaml)，根级治理脚本见 [package.json](E:\Governance-Demo\web\package.json)。

## 2. 结构定位

### 2.1 apps

`apps/*` 只承载正式业务应用。

每个应用内部允许放置：

- 页面
- 路由
- layout
- 应用级配置
- 应用私有 mock
- 应用私有状态与业务逻辑

不应在 `apps/*` 之间直接复制公共实现。

### 2.2 packages

`packages/*` 承载跨应用复用能力。

当前职责建议如下：

- `packages/api`: 请求封装、接口方法、通用 API 类型
- `packages/components`: 可复用组件与页面级通用 UI
- `packages/i18n`: 国际化资源与语言能力
- `packages/mocks`: 已收编的共享 mock 资源
- `packages/utils`: 通用工具函数与轻量辅助能力

## 3. 当前已确认的工程规则

### 3.1 保留的别名策略

后续继续保留以下跨包别名：

- `@api`
- `@utils`
- `@components`
- `@i18n`

应用内部私有代码继续使用 `@/`。

### 3.2 已确认的事实

- `bms` 是正式应用，必须保留
- 当前四端就是正式结构，不再回退为单体目录
- `shared/` 已经退出当前结构，不应重新引入
- `shared/account` 已迁移到 `packages/mocks`

## 4. 开发约束

### 4.1 目录约束

后续开发必须遵守：

- 禁止重新创建 `shared/` 这类 workspace 外公共目录
- 禁止把跨端复用代码继续散落在 `apps/*`
- 禁止为了临时开发方便，把共享逻辑复制到多个应用里

### 4.2 共享代码准入规则

满足以下条件的代码，应优先进入 `packages/*`：

- 至少两个应用会复用
- 与具体页面无强绑定
- 具备明确边界，能单独维护

满足以下条件的代码，应留在应用内：

- 只服务单端业务
- 强依赖该应用路由或 layout
- 明显属于页面私有逻辑

### 4.3 依赖约束

- 新增跨包依赖时，必须同步更新对应 `package.json`
- 不要只改 `tsconfig paths` 而不补 workspace 依赖声明
- 共享包内尽量避免引入不必要的大型运行时依赖
- 如果只是类型或开发工具依赖，优先放到 `devDependencies`

### 4.4 mock 约束

- 共享 mock 优先放在 `packages/mocks`
- 应用私有 mock 继续留在各自 `apps/*/mock`
- 不要再把共享 mock 放回应用目录或根目录杂项文件夹

## 5. 推荐开发流程

### 5.1 开发前

开始一个新功能前，先判断：

1. 这个功能属于哪一端
2. 是否存在跨端复用
3. 是否需要进入 `packages/*`
4. 是否需要补依赖声明

### 5.2 开发中

优先顺序建议如下：

1. 先落应用代码
2. 再抽共享能力
3. 最后补文档和治理脚本

不要一开始就过度抽象。

### 5.3 开发后最低校验

默认最低校验命令：

```bash
pnpm run typecheck
pnpm run lint
pnpm run build
```

如果只想校验共享包侧，可使用：

```bash
pnpm run typecheck:packages
pnpm run lint:packages
pnpm run build:packages
```

如果只开发单端，可补充使用对应应用命令，例如：

```bash
pnpm dev:bms
pnpm dev:govern
pnpm dev:portal
pnpm dev:screen
```

## 6. 当前工程基线

截至当前这轮调整，已经确认：

- 根级 `typecheck` 可通过
- 根级 `lint` 可通过
- 根级 `build` 可通过
- 根级 `test` 可通过，但测试运行中仍存在旧测试链路警告

这意味着：

- 工程已经具备正式承接业务开发的条件
- 后续重点应转向业务开发，而不是继续停留在 monorepo 调整本身

## 7. 当前非阻塞遗留项

以下问题不阻塞正式开发，但后续可以单独治理：

- 旧历史文档存在编码问题
- `packages/*` 除 `typecheck` 外，部分包仍未统一实现自己的 `lint / test / build`
- 测试链路里仍有 React 19 下的 `act(...)` 警告与 open handle 提示

这些项建议作为后续治理任务单独处理，不要和业务开发混在一起。

## 8. 后续开发的硬规则

后续如果继续扩展工程，请默认遵守以下规则：

- 新公共能力优先放 `packages/*`
- 不再新增 `shared/`
- 不破坏 `@api / @utils / @components / @i18n` 现有别名模式
- 不随意调整四端结构
- 新增依赖时同步维护 workspace 声明
- 提交前至少保证 `typecheck + lint + build`

## 9. 简短结论

当前工程已经可以正式进入开发阶段。

如果后续没有新的架构级需求，应把 monorepo 视为当前稳定底座，在这个基础上继续做业务功能，而不是再次发散式调整目录结构。
