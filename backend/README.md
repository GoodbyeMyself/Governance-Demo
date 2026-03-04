# 数据治理后端（Data Governance Backend）

这是一个面向数据治理场景的后端服务，基于 Spring Boot 构建。  
项目在结构上采用“按业务模块拆分 + 公共能力下沉”的方式，方便后续长期扩展。

---

## 一、这个后端是做什么的？（先给新手一个整体认知）

你可以把这个项目理解成三层：

- **接口层（Controller）**：接收前端请求、返回结果。
- **业务层（Service）**：写核心业务逻辑（校验、规则、流程）。
- **数据层（Repository + Entity）**：和数据库打交道。

再配合一些**公共能力**（统一返回、全局异常、跨域配置等），就形成了一个比较标准的企业后端工程。

---

## 二、工程目录结构（新手友好版）

### 1）Java 主代码目录

```text
src/main/java/com/governance/platform
├─ GovernancePlatformApplication.java     # Spring Boot 启动类（程序入口）
├─ shared                                 # 公共层：全局通用能力，跨模块复用
│  ├─ api
│  │  └─ ApiResponse.java                 # 统一接口响应体（success/message/data）
│  ├─ config
│  │  └─ WebConfig.java                   # Web 全局配置（如 CORS、拦截器注册等）
│  ├─ exception
│  │  ├─ GlobalExceptionHandler.java      # 全局异常处理器，统一错误响应
│  │  └─ ResourceNotFoundException.java   # 通用“资源不存在”异常
│  └─ web
│     ├─ ClientRequestContextHolder.java     # 请求上下文保存（线程内）
│     ├─ ClientRequestHeaderInterceptor.java # 请求头拦截/透传
│     ├─ ClientRequestHeaderProvider.java    # 统一读取请求头
│     └─ ClientRequestHeaders.java           # 请求头对象封装
└─ modules                                # 业务模块层（每个模块独立）
   ├─ datasource                          # 数据源管理模块
   │  ├─ controller
   │  │  └─ DataSourceController.java         # 数据源 API（增删改查入口）
   │  ├─ dto
   │  │  ├─ DataSourceRequest.java            # 创建/更新数据源请求参数
   │  │  └─ DataSourceResponse.java           # 数据源响应对象
   │  ├─ entity
   │  │  ├─ DataSourceInfo.java               # 数据源实体（映射数据库表）
   │  │  └─ DataSourceType.java               # 数据源类型枚举
   │  ├─ repository
   │  │  └─ DataSourceRepository.java         # 数据源数据库访问接口（JPA）
   │  ├─ service
   │  │  ├─ DataSourceService.java            # 数据源业务接口
   │  │  └─ impl
   │  │     └─ DataSourceServiceImpl.java     # 数据源业务实现
   │  └─ exception
   │     ├─ DataSourceInUseException.java     # 数据源被占用异常
   │     └─ DuplicateDataSourceException.java # 数据源重名异常
   ├─ metadata                            # 元数据采集任务模块
   │  ├─ controller
   │  │  └─ MetadataCollectionTaskController.java      # 元数据任务 API
   │  ├─ dto
   │  │  ├─ MetadataCollectionTaskRequest.java         # 元数据任务请求参数
   │  │  └─ MetadataCollectionTaskResponse.java        # 元数据任务响应对象
   │  ├─ entity
   │  │  ├─ MetadataCollectionTask.java                # 元数据任务实体
   │  │  ├─ MetadataCollectionScheduleType.java        # 调度类型枚举
   │  │  ├─ MetadataCollectionScope.java               # 采集范围枚举
   │  │  └─ MetadataCollectionStrategy.java            # 采集策略枚举
   │  ├─ repository
   │  │  └─ MetadataCollectionTaskRepository.java      # 元数据任务数据库访问接口
   │  ├─ service
   │  │  ├─ MetadataCollectionTaskService.java         # 元数据任务业务接口
   │  │  └─ impl
   │  │     └─ MetadataCollectionTaskServiceImpl.java  # 元数据任务业务实现
   │  └─ exception
   │     └─ DuplicateMetadataCollectionTaskException.java # 元数据任务重名异常
   └─ workbench                           # 工作台模块（首页统计/概览）
      ├─ controller
      │  └─ WorkbenchController.java          # 工作台 API
      ├─ dto
      │  └─ WorkbenchOverviewResponse.java    # 工作台概览响应对象
      └─ service
         ├─ WorkbenchService.java             # 工作台业务接口
         └─ impl
            └─ WorkbenchServiceImpl.java      # 工作台业务实现
```

---

### 2）资源与配置目录

```text
src/main/resources
├─ application.yml   # 应用配置（端口、数据库连接、JPA 等）
└─ schema.sql        # 数据库建表脚本（应用启动时可初始化表结构）
```

---

### 3）其他后端相关文件

```text
backend
├─ pom.xml          # Maven 项目配置：依赖、插件、打包方式
└─ sql/init.sql     # 额外初始化 SQL（可用于手动初始化数据）
```

---

## 三、每一层到底干什么？（按请求链路理解）

以“新增数据源”为例：

1. 前端调用 `POST /api/data-sources`
2. `DataSourceController` 接收请求，并把参数映射成 `DataSourceRequest`
3. Controller 调用 `DataSourceService`
4. `DataSourceServiceImpl` 做业务校验（例如是否重名）
5. 通过 `DataSourceRepository` 写入数据库
6. 返回 `DataSourceResponse`
7. 最外层再包装成 `ApiResponse` 返回给前端
8. 如果过程中抛异常，`GlobalExceptionHandler` 统一处理成友好的错误响应

> 这就是典型的：Controller -> Service -> Repository -> DB

---

## 四、模块开发规则（后续扩展时请遵守）

- 新业务统一加在 `modules/<module-name>/...` 下。
- 跨模块复用的能力统一放 `shared`。
- 不要把“业务逻辑”直接写在根包下。
- **Controller 只做参数接收和返回，不写复杂业务。**
- **复杂规则放 Service，不放 Repository。**

---

## 五、技术栈

- Spring Boot（REST API 框架）
- Spring Data JPA（数据库访问）
- MySQL（关系型数据库）
- Lombok（减少样板代码）

---

## 六、运行前准备

- JDK 17+
- Maven 3.9+
- MySQL 8+

---

## 七、启动步骤（一步一步）

### 1）创建数据库

```sql
CREATE DATABASE governance_demo DEFAULT CHARACTER SET utf8mb4;
```

### 2）配置数据库连接

编辑 `src/main/resources/application.yml`：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 3）启动项目

```bash
mvn spring-boot:run
```

启动成功后，后端会监听你配置的端口（常见是 `8080`）。

---

## 八、已提供的核心 API

### 1）数据源管理（Base Path: `/api/data-sources`）

- `POST /api/data-sources`：新增数据源
- `DELETE /api/data-sources/{id}`：删除数据源
- `PUT /api/data-sources/{id}`：更新数据源
- `GET /api/data-sources`：查询全部数据源

#### POST 请求示例

```json
{
  "name": "MySQL-Prod",
  "type": "DATABASE",
  "connectionUrl": "jdbc:mysql://localhost:3306/demo",
  "username": "root",
  "password": "root",
  "description": "Production MySQL"
}
```

#### 成功响应示例

```json
{
  "success": true,
  "message": "Data source created",
  "data": {
    "id": 1,
    "name": "MySQL-Prod",
    "type": "DATABASE",
    "connectionUrl": "jdbc:mysql://localhost:3306/demo",
    "username": "root",
    "description": "Production MySQL",
    "createdAt": "2026-03-01T10:00:00",
    "updatedAt": "2026-03-01T10:00:00"
  }
}
```

#### 失败响应示例（名称重复）

```json
{
  "success": false,
  "message": "Data source name already exists: MySQL-Prod",
  "data": null
}
```

### 2）元数据采集任务模块（提示）

项目中也已经有 `metadata` 模块，包含任务管理相关的增删改查能力（具体接口可在 `MetadataCollectionTaskController.java` 中查看）。

### 3）工作台模块（提示）

项目中有 `workbench` 模块，主要用于首页统计与概览数据（具体接口可在 `WorkbenchController.java` 中查看）。

---

## 九、前端如何对接

前端（React）可以使用 `fetch` 或 `axios` 调用上述 API，然后把返回的 `data` 绑定到表格或列表组件。

---

## 十、给后端新手的学习建议（按顺序）

建议你按下面顺序看代码：

1. `GovernancePlatformApplication.java`（了解项目如何启动）
2. `shared/api/ApiResponse.java`（理解统一返回格式）
3. `modules/datasource/controller/DataSourceController.java`（先看接口）
4. `modules/datasource/service/impl/DataSourceServiceImpl.java`（再看业务）
5. `modules/datasource/repository/DataSourceRepository.java` + `entity/DataSourceInfo.java`（最后看数据落库）
6. `shared/exception/GlobalExceptionHandler.java`（理解异常如何统一返回）

只要你把 `datasource` 这个模块跑通，再看 `metadata`、`workbench` 会轻松很多。
