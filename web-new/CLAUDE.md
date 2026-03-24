# 项目约定

- 所有与 antd / ant-design-pro / pro-components 相关的样式复写，统一放在 `src/assets/less/antd.less` 中维护。
- `src/global.less` 仅负责全局基础样式及对 `src/assets/less/antd.less` 的引入，不再分散编写 antd 组件覆盖样式。
