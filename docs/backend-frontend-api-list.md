# 后端对前端联调接口清单

## 1. 文档说明

- 文档范围：仅统计前端可通过网关访问的对外业务接口
- 统计时间：2026-04-01
- 网关访问前缀：`/api/**`
- 不包含内容：`/internal/**` 内部服务调用接口
- 接口总数：`38`

## 2. 权限说明

| 标识 | 说明 |
| --- | --- |
| 匿名 | 无需登录即可访问 |
| 已登录 | 需要已登录状态 |
| 管理员 | 需要管理员权限，代码中明确标注 `hasRole('ADMIN')` |

## 3. 认证中心

服务模块：`auth-center`

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/auth-center/captcha` | 匿名 | 获取图形验证码 |
| 2 | POST | `/api/auth-center/email-codes/send` | 匿名 | 发送邮箱验证码 |
| 3 | POST | `/api/auth-center/register` | 匿名 | 用户注册 |
| 4 | POST | `/api/auth-center/login` | 匿名 | 用户登录 |
| 5 | POST | `/api/auth-center/password/reset` | 匿名 | 重置密码 |
| 6 | GET | `/api/auth-center/me` | 已登录 | 获取当前登录用户信息 |
| 7 | PUT | `/api/auth-center/me/profile` | 已登录 | 修改当前登录用户资料 |
| 8 | POST | `/api/auth-center/logout` | 已登录 | 退出登录 |

## 4. 后台管理 BMS

服务模块：`bms-service`

### 4.1 用户与权限

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 9 | GET | `/api/bms/users` | 管理员 | 查询用户列表 |
| 10 | PUT | `/api/bms/users/{id}/role` | 管理员 | 修改用户角色 |
| 11 | GET | `/api/bms/roles` | 管理员 | 查询角色列表 |
| 12 | GET | `/api/bms/permissions` | 管理员 | 查询权限列表 |

### 4.2 角色定义管理

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 13 | GET | `/api/bms/role-definitions` | 管理员 | 查询角色定义列表 |
| 14 | PUT | `/api/bms/role-definitions/{roleCode}` | 管理员 | 修改角色定义 |

## 5. 数据源管理

服务模块：`data-source`

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 15 | POST | `/api/data-source` | 已登录 | 新增数据源 |
| 16 | DELETE | `/api/data-source/{id}` | 已登录 | 删除数据源 |
| 17 | PUT | `/api/data-source/{id}` | 已登录 | 更新数据源 |
| 18 | GET | `/api/data-source` | 已登录 | 查询全部数据源 |
| 19 | GET | `/api/data-source/{id}` | 已登录 | 查询数据源详情 |

## 6. 元数据管理

服务模块：`data-metadata`

### 6.1 元数据采集任务

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 20 | POST | `/api/data-metadata/tasks` | 已登录 | 新增元数据采集任务 |
| 21 | GET | `/api/data-metadata/tasks/{id}` | 已登录 | 查询元数据采集任务详情 |
| 22 | PUT | `/api/data-metadata/tasks/{id}` | 已登录 | 更新元数据采集任务 |
| 23 | DELETE | `/api/data-metadata/tasks/{id}` | 已登录 | 删除元数据采集任务 |
| 24 | GET | `/api/data-metadata/tasks` | 已登录 | 查询全部元数据采集任务 |

### 6.2 工作台

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 25 | GET | `/api/data-metadata/workbench/overview` | 已登录 | 获取工作台概览统计 |

## 7. IoT 设备管理

服务模块：`iot-device`

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 26 | POST | `/api/iot-device` | 已登录 | 新增 IoT 设备 |
| 27 | PUT | `/api/iot-device/{id}` | 已登录 | 更新 IoT 设备 |
| 28 | DELETE | `/api/iot-device/{id}` | 已登录 | 删除 IoT 设备 |
| 29 | GET | `/api/iot-device` | 已登录 | 查询全部 IoT 设备 |
| 30 | GET | `/api/iot-device/{id}` | 已登录 | 查询 IoT 设备详情 |

## 8. IoT 采集与遥测

服务模块：`iot-collection`

### 8.1 IoT 采集任务

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 31 | POST | `/api/iot-collection/tasks` | 已登录 | 新增 IoT 采集任务 |
| 32 | PUT | `/api/iot-collection/tasks/{id}` | 已登录 | 更新 IoT 采集任务 |
| 33 | DELETE | `/api/iot-collection/tasks/{id}` | 已登录 | 删除 IoT 采集任务 |
| 34 | GET | `/api/iot-collection/tasks` | 已登录 | 查询全部 IoT 采集任务 |
| 35 | GET | `/api/iot-collection/tasks/{id}` | 已登录 | 查询 IoT 采集任务详情 |

### 8.2 IoT 遥测查询

| 序号 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 36 | GET | `/api/iot-collection/telemetry/latest/{deviceId}` | 已登录 | 查询设备最新遥测数据 |
| 37 | GET | `/api/iot-collection/telemetry/history` | 已登录 | 查询设备历史遥测数据，参数包含 `deviceId`、`metricCode`、`startTime`、`endTime` |
| 38 | GET | `/api/iot-collection/telemetry/overview` | 已登录 | 查询遥测概览统计 |

## 9. 统计汇总

| 模块 | 接口数 |
| --- | --- |
| 认证中心 | 8 |
| 后台管理 BMS | 6 |
| 数据源管理 | 5 |
| 元数据管理 | 6 |
| IoT 设备管理 | 5 |
| IoT 采集与遥测 | 8 |
| 合计 | 38 |

## 10. 说明补充

- 以上路径均来自各业务服务 Controller 的对外映射定义
- 匿名接口与默认鉴权规则依据公共安全配置 `common/service-support` 中的 `SecurityConfig`
- BMS 相关 6 个接口在 Controller 上显式使用了 `@PreAuthorize("hasRole('ADMIN')")`
