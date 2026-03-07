# 数据治理后端（Data Governance Backend）

本目录是 `Governance-Demo` 的后端服务，基于 `Spring Boot 3 + Spring Security + Spring Data JPA + MySQL`。

---

## 1. 技术栈与运行要求

- Java: `17+`
- Maven: `3.9+`
- MySQL: `8+`
- Spring Boot: `3.3.5`
- 鉴权: JWT（`jjwt 0.12.6`）

---

## 2. 项目目录结构（详细版）

## 2.1 根目录与资源层

```text
backend/
├─ pom.xml
├─ README.md
├─ sql/
│  └─ init.sql
└─ src/
   └─ main/
      ├─ java/com/governance/platform/
      └─ resources/
         ├─ application.yml
         └─ schema.sql
```

| 路径 | 简要描述 |
|---|---|
| `pom.xml` | Maven 项目配置，定义依赖、Java 版本与打包插件。 |
| `sql/init.sql` | 手动初始化脚本，包含 `governance_demo` 建库与三张核心表建表语句。 |
| `src/main/resources/application.yml` | 后端运行配置：端口、数据库、JPA、JWT、管理员初始化参数、CORS 参数。 |
| `src/main/resources/schema.sql` | 应用启动时执行的建表脚本（配合 `spring.sql.init.mode=always`）。 |
| `GovernancePlatformApplication.java` | Spring Boot 启动入口。 |

## 2.2 Java 包结构（按模块与文件说明）

### 2.2.1 `modules/authcenter`（认证中心）

| 文件 | 简要描述 |
|---|---|
| `bootstrap/AuthCenterAdminInitializer.java` | 启动时初始化内置管理员；如用户已存在仅校正角色/状态，不重置密码。 |
| `controller/AuthCenterController.java` | 认证中心接口：注册、登录、当前用户、登出、用户列表、角色变更。 |
| `dto/AuthCenterLoginRequest.java` | 登录请求 DTO（用户名/密码）。 |
| `dto/AuthCenterLoginResponse.java` | 登录响应 DTO（token、过期时间、用户信息）。 |
| `dto/AuthCenterRegisterRequest.java` | 注册请求 DTO（含用户名/密码复杂度/邮箱/手机号校验）。 |
| `dto/AuthCenterUpdateUserRoleRequest.java` | 管理员修改角色请求 DTO。 |
| `dto/AuthCenterUserProfileResponse.java` | 用户档案响应 DTO。 |
| `entity/AuthCenterUser.java` | 用户实体，映射 `sys_users` 表。 |
| `entity/AuthCenterUserRole.java` | 用户角色枚举（`USER`/`ADMIN`）。 |
| `entity/AuthCenterUserStatus.java` | 用户状态枚举（`ENABLED`/`DISABLED`）。 |
| `exception/AuthCenterAuthenticationException.java` | 认证失败异常（401）。 |
| `exception/AuthCenterDuplicateUserException.java` | 用户重复异常（409）。 |
| `exception/AuthCenterOperationException.java` | 业务操作异常（400）。 |
| `exception/AuthCenterUserDisabledException.java` | 用户禁用异常（403）。 |
| `repository/AuthCenterUserRepository.java` | 用户仓储，提供按用户名/邮箱/手机号查询与唯一性判断。 |
| `service/AuthCenterService.java` | 认证中心服务接口。 |
| `service/AuthCenterUserDetailsService.java` | 对接 Spring Security 的用户加载实现。 |
| `service/impl/AuthCenterServiceImpl.java` | 认证中心核心业务实现（注册、登录、角色变更、管理员最少保留一个）。 |

### 2.2.2 `modules/datasource`（数据源管理）

| 文件 | 简要描述 |
|---|---|
| `controller/DataSourceController.java` | 数据源 CRUD 接口。 |
| `dto/DataSourceRequest.java` | 数据源创建/更新请求 DTO。 |
| `dto/DataSourceResponse.java` | 数据源响应 DTO。 |
| `entity/DataSourceInfo.java` | 数据源实体，映射 `data_sources` 表。 |
| `entity/DataSourceType.java` | 数据源类型枚举（`DATABASE`/`FILE_SYSTEM`）。 |
| `exception/DataSourceInUseException.java` | 被任务引用时禁止删除的异常（409）。 |
| `exception/DuplicateDataSourceException.java` | 数据源重名异常（409）。 |
| `repository/DataSourceRepository.java` | 数据源仓储，提供名称唯一性检查与统计查询。 |
| `service/DataSourceService.java` | 数据源服务接口。 |
| `service/impl/DataSourceServiceImpl.java` | 数据源业务实现（重名校验、引用校验、增删改查）。 |

### 2.2.3 `modules/metadata`（元数据采集任务）

| 文件 | 简要描述 |
|---|---|
| `controller/MetadataCollectionTaskController.java` | 元数据采集任务 CRUD 接口。 |
| `dto/MetadataCollectionTaskRequest.java` | 任务创建/更新请求 DTO（含字段校验）。 |
| `dto/MetadataCollectionTaskResponse.java` | 任务响应 DTO。 |
| `entity/MetadataCollectionTask.java` | 任务实体，映射 `metadata_collection_tasks` 表。 |
| `entity/MetadataCollectionStrategy.java` | 采集策略枚举（`FULL`/`INCREMENTAL`）。 |
| `entity/MetadataCollectionScope.java` | 采集范围枚举（`SCHEMA`/`TABLE`）。 |
| `entity/MetadataCollectionScheduleType.java` | 调度类型枚举（`MANUAL`/`CRON`）。 |
| `exception/DuplicateMetadataCollectionTaskException.java` | 任务名称重复异常（409）。 |
| `repository/MetadataCollectionTaskRepository.java` | 任务仓储，提供按任务名/更新时间/统计查询。 |
| `service/MetadataCollectionTaskService.java` | 元数据任务服务接口。 |
| `service/impl/MetadataCollectionTaskServiceImpl.java` | 任务业务实现（重名校验、cron 校验、configJson 合法性校验）。 |

### 2.2.4 `modules/workbench`（工作台）

| 文件 | 简要描述 |
|---|---|
| `controller/WorkbenchController.java` | 工作台概览接口。 |
| `dto/WorkbenchOverviewResponse.java` | 工作台响应 DTO（总量、趋势、最近更新项）。 |
| `service/WorkbenchService.java` | 工作台服务接口。 |
| `service/impl/WorkbenchServiceImpl.java` | 统计聚合实现（计数、7 天趋势、最近 5 条）。 |

### 2.2.5 `shared`（公共能力）

| 文件 | 简要描述 |
|---|---|
| `api/ApiResponse.java` | 全局统一响应体（`success/message/data`）。 |
| `config/WebConfig.java` | Web 配置：CORS 来源控制 + 请求头拦截器注册。 |
| `exception/GlobalExceptionHandler.java` | 全局异常映射到统一 JSON 响应。 |
| `exception/ResourceNotFoundException.java` | 通用资源不存在异常（404）。 |
| `security/SecurityConfig.java` | 安全总配置：放行路径、JWT 过滤链、无状态会话。 |
| `security/JwtAuthenticationFilter.java` | 从 `Authorization` 解析 JWT 并建立 SecurityContext。 |
| `security/JwtTokenService.java` | JWT 生成、解析、有效性验证。 |
| `security/RestAuthenticationEntryPoint.java` | 未认证入口处理（401 JSON）。 |
| `security/RestAccessDeniedHandler.java` | 无权限入口处理（403 JSON）。 |
| `web/ClientRequestHeaders.java` | 请求头模型，定义 `X-Request-Id` 等常量。 |
| `web/ClientRequestHeaderInterceptor.java` | 请求进入时提取/补齐链路头，响应回写 `X-Request-Id`。 |
| `web/ClientRequestHeaderProvider.java` | 业务层读取请求上下文头信息的便捷组件。 |
| `web/ClientRequestContextHolder.java` | 基于 `ThreadLocal` 的请求上下文持有器。 |

## 2.3 分层职责速览

- `modules/*/controller`: API 入口、参数校验、调用 service
- `modules/*/service`: 核心业务规则
- `modules/*/repository`: JPA 访问数据库
- `modules/*/entity`: 表结构映射
- `modules/*/dto`: 入参/出参对象
- `shared/security`: JWT 鉴权链路
- `shared/exception`: 全局错误处理
- `shared/web`: 请求头上下文与链路追踪

---

## 3. 核心业务模块说明

## 3.1 authcenter（认证中心）

职责：

- 注册 / 登录
- 查询当前用户
- 管理员查看用户列表
- 管理员修改用户角色

关键点：

- 登录成功后返回 JWT token
- 启动时自动初始化内置管理员（仅首次创建，不会覆盖旧密码）

## 3.2 datasource（数据源管理）

职责：

- 数据源 CRUD

关键点：

- 名称唯一约束
- 被元数据任务引用时不允许删除

## 3.3 metadata（元数据采集任务）

职责：

- 任务 CRUD

关键点：

- 任务名称唯一
- `scheduleType=CRON` 时强制要求 `cronExpression`
- `configJson` 必须是合法 JSON

## 3.4 workbench（工作台）

职责：

- 返回统计指标（数据源/任务）
- 7 天趋势数据
- 最近更新项

---

## 4. 鉴权与权限模型

## 4.1 认证规则

- JWT 无状态（`SessionCreationPolicy.STATELESS`）
- 公开接口：
  - `POST /api/auth-center/register`
  - `POST /api/auth-center/login`
- 其他接口默认要求登录

## 4.2 授权规则

- 角色：`USER` / `ADMIN`
- 管理员接口：
  - `GET /api/auth-center/users`
  - `PUT /api/auth-center/users/{id}/role`

---

## 5. CORS 与请求头规范

## 5.1 CORS

配置键：

- `app.cors.allowed-origin-patterns`
- 对应环境变量：`APP_CORS_ALLOWED_ORIGIN_PATTERNS`

默认值：

- `http://localhost:*`
- `http://127.0.0.1:*`

## 5.2 链路请求头

支持透传：

- `X-Request-Id`
- `X-Request-Time`
- `X-Client-App`
- `X-Tenant-Id`
- `Authorization`

若请求未携带 `X-Request-Id`，后端会自动生成并写回响应头。

---

## 6. 配置说明（application.yml）

| 配置项 | 环境变量 | 默认值 |
|---|---|---|
| 数据库 URL | `DB_URL` | `jdbc:mysql://localhost:3306/governance_demo?...` |
| 数据库用户 | `DB_USERNAME` | `root` |
| 数据库密码 | `DB_PASSWORD` | `mayunlong` |
| 服务端口 | - | `8080` |
| CORS 来源 | `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | `http://localhost:*,http://127.0.0.1:*` |
| JWT 密钥 | `AUTH_CENTER_JWT_SECRET` | `governance-auth-center-jwt-secret-please-change-it` |
| JWT 过期秒数 | `AUTH_CENTER_JWT_EXPIRE_SECONDS` | `86400` |
| 管理员用户名 | `AUTH_CENTER_ADMIN_USERNAME` | `admin` |
| 管理员密码 | `AUTH_CENTER_ADMIN_PASSWORD` | `Admin@123456` |
| 管理员昵称 | `AUTH_CENTER_ADMIN_NICKNAME` | `System Admin` |

---

## 7. 数据库初始化策略

当前配置：

- `spring.sql.init.mode=always`（执行 `schema.sql`）
- `spring.jpa.hibernate.ddl-auto=update`

手工初始化（可选）：

```bash
mysql -uroot -p < sql/init.sql
```

---

## 8. 本地运行与打包

开发启动：

```bash
cd backend
mvn spring-boot:run
```

打包：

```bash
cd backend
mvn -DskipTests clean package
```

产物：

- `target/data-governance-0.0.1-SNAPSHOT.jar`

运行：

```bash
java -jar target/data-governance-0.0.1-SNAPSHOT.jar
```

---

## 9. API 总览

统一响应体：

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

## 9.1 authcenter

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/auth-center/register` | 否 | 注册 |
| POST | `/api/auth-center/login` | 否 | 登录 |
| GET | `/api/auth-center/me` | 是 | 当前用户 |
| POST | `/api/auth-center/logout` | 是 | 退出 |
| GET | `/api/auth-center/users` | ADMIN | 用户列表 |
| PUT | `/api/auth-center/users/{id}/role` | ADMIN | 修改角色 |

## 9.2 datasource

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/data-sources` | 是 | 列表 |
| POST | `/api/data-sources` | 是 | 新建 |
| PUT | `/api/data-sources/{id}` | 是 | 更新 |
| DELETE | `/api/data-sources/{id}` | 是 | 删除 |

## 9.3 metadata

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/metadata-collection-tasks` | 是 | 列表 |
| GET | `/api/metadata-collection-tasks/{id}` | 是 | 详情 |
| POST | `/api/metadata-collection-tasks` | 是 | 新建 |
| PUT | `/api/metadata-collection-tasks/{id}` | 是 | 更新 |
| DELETE | `/api/metadata-collection-tasks/{id}` | 是 | 删除 |

## 9.4 workbench

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/workbench/overview` | 是 | 统计概览 |

---

## 10. 常见问题

## 10.1 登录返回 403（Invalid CORS request）

检查 `APP_CORS_ALLOWED_ORIGIN_PATTERNS` 是否包含当前前端 origin。

## 10.2 登录提示用户名或密码错误

若 `sys_users` 里已存在 `admin`，启动初始化不会重置其密码。  
需要手工改密或删除该账号后重启应用触发初始化。

## 10.3 请求体错误（400 Invalid request body）

通常是 JSON 格式错误或字段类型不匹配。
