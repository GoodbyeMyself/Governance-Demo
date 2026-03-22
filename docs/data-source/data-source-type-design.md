# data-source 微服务新增“数据源类型”管理能力实施说明

## 1. 目标说明

本文档用于指导在 `data-source` 微服务中手工实现“数据源类型”管理能力。

本阶段目标不是直接编写业务代码，而是输出一份可执行的改造说明，覆盖以下内容：
- 现有 `data-source` 模块结构分析
- 需要新增/修改的文件清单
- 数据库表设计建议
- 接口设计建议
- 业务逻辑组织建议
- 两阶段实施顺序
- 验证方式

结合当前代码库现状，`data-source` 模块采用的是 **Spring Boot + Spring Data JPA** 方案，不使用 MyBatis / mapper XML。现有数据源 CRUD 是本次改造的主要参考实现。

---

## 2. 当前 `data-source` 模块现状分析

### 2.1 现有核心结构

当前数据源管理主链路主要由以下文件组成：

- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/DataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/InternalDataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceService.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceServiceImpl.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/repository/DataSourceRepository.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceInfo.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceType.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceRequest.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceResponse.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/GlobalExceptionHandler.java`
- `microservice/service/data-source/src/main/resources/schema.sql`

### 2.2 现有实现特点

#### 1）Controller 风格
`DataSourceController.java` 当前提供标准 CRUD：
- `POST /api/data-source`
- `PUT /api/data-source/{id}`
- `DELETE /api/data-source/{id}`
- `GET /api/data-source`
- `GET /api/data-source/{id}`

返回风格统一为 `ApiResponse<T>`。

#### 2）Service 风格
`DataSourceServiceImpl.java` 中的典型实现方式为：
- 先做唯一性校验
- 使用 JPA Repository 操作实体
- Service 内部完成实体到 DTO 的转换
- 通过领域异常与 `GlobalExceptionHandler` 统一映射 HTTP 状态码

#### 3）Repository 风格
`DataSourceRepository.java` 继承 `JpaRepository`，通过方法命名规则完成查询，例如：
- `existsByName`
- `existsByNameAndIdNot`
- `countByType`
- `findTop5ByOrderByUpdatedAtDescIdDesc`
- `findByUpdatedAtBetween`

这说明当前项目是典型的 Spring Data JPA 风格，不需要新增 mapper XML。

#### 4）Entity 风格
`DataSourceInfo.java` 当前使用 JPA 注解映射 `data_sources` 表，时间字段采用：
- `@CreationTimestamp`
- `@UpdateTimestamp`

#### 5）类型字段现状
当前 `DataSourceInfo.type` 使用枚举：
- `DataSourceType.DATABASE`
- `DataSourceType.FILE_SYSTEM`

这意味着“数据源类型”目前是**写死在代码里的枚举值**，还不是可维护的业务字典。

#### 6）统计逻辑现状
`DataSourceServiceImpl.java` 的概览统计目前直接按枚举统计：
- `countByType(DataSourceType.DATABASE)`
- `countByType(DataSourceType.FILE_SYSTEM)`

因此如果未来类型变为可配置字典，统计逻辑需要评估是否继续保留固定项，或改造成动态字典统计。

---

## 3. 为什么当前不能直接做“类型 CRUD”

当前项目中的类型定义来自 `DataSourceType.java` 枚举，而不是数据库表。

这会带来几个限制：

1. 类型不能在运行时新增或删除
2. 前端无法通过接口维护类型列表
3. 数据源创建/编辑时只能使用代码中预定义的枚举值
4. 若后续新增第三类、第四类数据源，必须改代码、发版、重新部署

因此，如果要实现真正的“数据源类型管理”，就不能继续只依赖 enum，而应该引入独立的字典表。

---

## 4. 推荐方案

### 4.1 最终目标

推荐最终落地为以下结构：

1. 新增 `data_source_types` 表，维护数据源类型字典
2. 为该字典表提供完整 CRUD 接口
3. 将 `data_sources.type` 逐步演进为“存储类型编码 code”
4. 新增/修改数据源时，校验该类型编码是否存在
5. 删除类型前，校验是否仍被 `data_sources` 引用

### 4.2 建议采用的字段关联方式

推荐最终将数据源主表中的类型字段设计为：
- `data_sources.type_code` 存储 `data_source_types.code`

不建议优先改成 `type_id`，原因：
- 当前接口和前端语义更接近字符串编码
- 现有 enum 值本身已经具备稳定编码语义
- code 更适合跨服务传递和前端展示
- 平滑迁移成本更低

---

## 5. 数据库设计建议

### 5.1 新增表：`data_source_types`

建议表结构如下：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键，自增 |
| code | VARCHAR(50) | 类型编码，唯一，不为空 |
| name | VARCHAR(100) | 类型名称，唯一，不为空 |
| description | VARCHAR(500) | 描述，可空 |
| enabled | TINYINT(1) | 是否启用，默认 1 |
| sort_order | INT | 排序号，默认 0 |
| created_at | DATETIME(6) | 创建时间 |
| updated_at | DATETIME(6) | 更新时间 |

建议约束命名：
- 主键：`pk_data_source_types`
- 唯一约束：`uk_data_source_type_code`
- 唯一约束：`uk_data_source_type_name`

### 5.2 建议的建表 SQL

可按以下思路补充到 `microservice/service/data-source/src/main/resources/schema.sql` 中：

```sql
CREATE TABLE IF NOT EXISTS data_source_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_data_source_types PRIMARY KEY (id),
    CONSTRAINT uk_data_source_type_code UNIQUE (code),
    CONSTRAINT uk_data_source_type_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 5.3 默认初始化数据建议

建议初始化至少两条默认记录：

| code | name |
|---|---|
| DATABASE | 数据库 |
| FILE_SYSTEM | 文件系统 |

原因：
- 兼容当前已有的枚举语义
- 后续迁移 `data_sources` 时能平滑承接历史值

### 5.4 关于 `data_sources` 表的调整建议

当前表结构中字段为：
- `type VARCHAR(50) NOT NULL`

推荐最终改为：
- `type_code VARCHAR(50) NOT NULL`

如果希望降低一次性改造成本，也可以采用过渡方案：
- 数据库字段暂时仍叫 `type`
- 但语义改为“类型编码 code”

从清晰度来看，**更推荐最终改名为 `type_code`**。

### 5.5 是否加数据库外键

根据当前项目 `schema.sql` 风格，没有明显的大量显式外键约束习惯，因此建议：
- 先不增加数据库外键
- 在 service 层做删除前引用校验

这样更符合现有项目风格，也能降低迁移复杂度。

---

## 6. 需要新增的文件

### 6.1 Entity

建议新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceCategory.java`

也可以叫 `DataSourceTypeEntity.java`，但不建议与现有 enum `DataSourceType` 同名。

建议字段：
- `id`
- `code`
- `name`
- `description`
- `enabled`
- `sortOrder`
- `createdAt`
- `updatedAt`

实现风格应对齐 `DataSourceInfo.java`：
- `@Entity`
- `@Table`
- `@Column`
- `@CreationTimestamp`
- `@UpdateTimestamp`

### 6.2 Repository

建议新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/repository/DataSourceCategoryRepository.java`

建议方法：
- `boolean existsByCode(String code)`
- `boolean existsByName(String name)`
- `boolean existsByCodeAndIdNot(String code, Long id)`
- `boolean existsByNameAndIdNot(String name, Long id)`
- `List<DataSourceCategory> findAll(Sort sort)`
- `List<DataSourceCategory> findByEnabledTrueOrderBySortOrderAscIdAsc()`（可选）
- `Optional<DataSourceCategory> findByCode(String code)`（建议补充，供数据源创建/更新校验使用）

### 6.3 DTO

建议新增请求对象：
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceCategoryRequest.java`

建议字段：
- `code`
- `name`
- `description`
- `enabled`
- `sortOrder`

校验建议：
- `code`：必填，长度限制，推荐全大写英文+下划线
- `name`：必填，长度限制
- `description`：选填，长度限制
- `enabled`：必填
- `sortOrder`：可选或必填，建议设置默认值

建议新增响应对象：
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceCategoryResponse.java`

建议字段：
- `id`
- `code`
- `name`
- `description`
- `enabled`
- `sortOrder`
- `createdAt`
- `updatedAt`

### 6.4 Service

建议新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceCategoryService.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceCategoryServiceImpl.java`

建议接口定义：
- `createType(DataSourceCategoryRequest request)`
- `updateType(Long id, DataSourceCategoryRequest request)`
- `deleteType(Long id)`
- `getTypeById(Long id)`
- `getAllTypes()`
- `getEnabledTypes()`（可选）

### 6.5 Controller

建议新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/DataSourceCategoryController.java`

可选新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/InternalDataSourceCategoryController.java`

### 6.6 Exception

建议新增：
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/DuplicateDataSourceCategoryException.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/DataSourceCategoryInUseException.java`

---

## 7. 需要修改的现有文件

### 7.1 第一阶段只做类型字典 CRUD 时

第一阶段建议尽量不动现有数据源主链路，只修改：

- `microservice/service/data-source/src/main/resources/schema.sql`
- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/GlobalExceptionHandler.java`

并新增前述类型相关 entity / repository / dto / service / controller / exception 文件。

### 7.2 第二阶段联动数据源主业务时

建议修改以下文件：

- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceInfo.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceRequest.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceResponse.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceServiceImpl.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/repository/DataSourceRepository.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceType.java`
- 如有需要，`DataSourceStatsOverviewResponse.java` 也可能需要跟随调整展示字段语义

---

## 8. 接口设计建议

### 8.1 类型管理对外接口

建议路径前缀：
- `/api/data-source/types`

#### 1）新增类型
- `POST /api/data-source/types`
- 请求体：`DataSourceCategoryRequest`
- 返回：`ApiResponse<DataSourceCategoryResponse>`

#### 2）修改类型
- `PUT /api/data-source/types/{id}`
- 请求体：`DataSourceCategoryRequest`
- 返回：`ApiResponse<DataSourceCategoryResponse>`

#### 3）删除类型
- `DELETE /api/data-source/types/{id}`
- 返回：`ApiResponse<Void>`

#### 4）查询类型列表
- `GET /api/data-source/types`
- 返回：`ApiResponse<List<DataSourceCategoryResponse>>`

#### 5）查询类型详情
- `GET /api/data-source/types/{id}`
- 返回：`ApiResponse<DataSourceCategoryResponse>`

#### 6）可选：查询启用类型列表
- `GET /api/data-source/types/enabled`
- 返回：`ApiResponse<List<DataSourceCategoryResponse>>`

这个接口适合给前端新增/编辑数据源页面提供下拉选项。

### 8.2 可选内部接口

如其他服务需要读取启用类型列表，可新增：
- `/internal/data-source/types`
- `/internal/data-source/types/enabled`
- `/internal/data-source/types/{id}`

如果当前仅用于前端页面管理，可以先不做内部接口。

---

## 9. 业务逻辑设计建议

### 9.1 类型新增逻辑

在 `DataSourceCategoryServiceImpl` 中建议执行以下步骤：

1. 校验 `code` 是否重复
2. 校验 `name` 是否重复
3. 组装实体
4. 保存实体
5. 转换为响应 DTO 并返回

### 9.2 类型更新逻辑

建议步骤：

1. 按 ID 查询类型，不存在则抛 `ResourceNotFoundException`
2. 校验排除自身后的 `code` 唯一
3. 校验排除自身后的 `name` 唯一
4. 更新字段
5. 保存并返回响应 DTO

### 9.3 类型删除逻辑

建议步骤：

1. 按 ID 查询类型，不存在则抛 `ResourceNotFoundException`
2. 根据该类型 `code` 检查 `data_sources` 是否仍有引用
3. 若有引用，抛 `DataSourceCategoryInUseException`
4. 若无引用，允许删除

为此，建议在 `DataSourceRepository` 中补充一个引用判断方法，例如：
- `existsByType(String typeCode)`

如果后续实体字段改名为 `typeCode`，则方法名同步改为：
- `existsByTypeCode(String typeCode)`

### 9.4 类型列表逻辑

建议排序规则：
- `sortOrder ASC, id ASC`

这样更方便做前端稳定展示。

### 9.5 数据源创建/更新时的类型校验

在第二阶段改造 `DataSourceServiceImpl` 时，建议增加：

1. 从请求中拿到 `typeCode`
2. 查询 `data_source_types` 是否存在该 code
3. 若不存在，抛 `IllegalArgumentException` 或更明确的领域异常
4. 若存在但 `enabled = false`，根据设计决定是否禁止新建/修改时使用
5. 校验通过后再保存数据源

建议从业务一致性角度，**禁用类型后不允许再被新建/编辑数据源使用**。

---

## 10. 现有数据源主链路的具体改造方向

### 10.1 `DataSourceInfo.java`

当前：
- `private DataSourceType type;`
- `@Enumerated(EnumType.STRING)`

建议最终改为：
- `private String typeCode;`

如果数据库字段名也同步调整，则建议：
- `@Column(name = "type_code", nullable = false, length = 50)`

### 10.2 `DataSourceRequest.java`

当前请求中的 `type` 是枚举。

建议改为字符串编码，例如：
- `private String typeCode;`

并增加校验约束：
- 必填
- 长度限制
- 可选增加格式限制

### 10.3 `DataSourceResponse.java`

当前响应中的 `type` 是枚举。

建议改造为：
- `typeCode`
- `typeName`（建议新增，方便前端直接展示）

推荐最终响应包含：
- `id`
- `name`
- `typeCode`
- `typeName`
- `connectionUrl`
- `username`
- `description`
- `createdAt`
- `updatedAt`

### 10.4 `DataSourceServiceImpl.java`

需要调整的要点：

1. 创建和更新时，增加类型字典存在性校验
2. 实体保存时不再写 enum，而是写 type code
3. DTO 转换时，按需要补 `typeName`
4. 统计逻辑中，暂时评估是否继续保留固定两类统计

### 10.5 `DataSourceRepository.java`

当前：
- `countByType(DataSourceType type)`

后续可根据最终字段定义调整为：
- `countByTypeCode(String typeCode)`
- `existsByTypeCode(String typeCode)`

### 10.6 `DataSourceType.java`

这个枚举在完成迁移后，建议：
- 删除
或
- 先标记为废弃，作为过渡阶段兼容实现

如果希望控制改造风险，建议分两步走：
1. 先做类型字典 CRUD
2. 再把数据源主业务从 enum 切到字典 code

---

## 11. 异常处理建议

### 11.1 新增异常类

建议新增：
- `DuplicateDataSourceCategoryException`
- `DataSourceCategoryInUseException`

风格参考现有：
- `DuplicateDataSourceException.java`
- `DataSourceInUseException.java`

### 11.2 修改 `GlobalExceptionHandler.java`

当前 `GlobalExceptionHandler.java` 已处理：
- 重复数据源异常 -> `409 Conflict`
- 数据源被占用异常 -> `409 Conflict`
- 资源不存在 -> `404`
- 参数校验失败 -> `400`
- 参数类型错误 -> `400`
- 非法参数 -> `400`

建议为新增的类型领域异常同样补充 `409 Conflict` 映射，保持整体接口风格一致。

---

## 12. 统计逻辑调整建议

当前 `DataSourceServiceImpl.java` 中首页概览统计按固定枚举分类：
- 数据库
- 文件系统

改造后有两个选择：

### 方案 A：先保持现状
仍然保留固定两项统计，只是底层改成按 code 统计：
- `DATABASE`
- `FILE_SYSTEM`

优点：
- 改造范围可控
- 不影响现有工作台展示
- 可以先把 CRUD 主功能做通

### 方案 B：改造成动态字典统计
按 `data_source_types` 动态生成统计项。

优点：
- 更符合“类型可配置”的最终态

缺点：
- 会牵涉工作台/前端展示结构调整
- 改造面更大

**建议先采用方案 A。**
先完成类型字典 CRUD 与数据源联动校验，再评估首页概览是否要动态化。

---

## 13. 推荐实施顺序

### 第一阶段：先新增类型字典 CRUD，不改现有数据源主逻辑

建议顺序：

1. 在 `schema.sql` 中新增 `data_source_types` 表
2. 新增类型实体、仓储、DTO、Service、Controller、异常类
3. 修改 `GlobalExceptionHandler.java`
4. 打通以下接口：
   - 新增类型
   - 修改类型
   - 删除类型
   - 查询类型列表
   - 查询类型详情
   - 可选：查询启用类型列表
5. 验证类型管理接口独立可用

这一阶段完成后，系统已经具备“类型字典管理”能力，但原有数据源 CRUD 仍继续使用 enum。

### 第二阶段：改造数据源主业务，切换到字典 code

建议顺序：

1. 为 `data_source_types` 初始化默认值 `DATABASE` / `FILE_SYSTEM`
2. 调整 `data_sources.type` 字段语义，最好迁移为 `type_code`
3. 修改 `DataSourceInfo.java`
4. 修改 `DataSourceRequest.java`
5. 修改 `DataSourceResponse.java`
6. 修改 `DataSourceServiceImpl.java`，补类型存在性校验
7. 修改 `DataSourceRepository.java`
8. 评估并删除/废弃 `DataSourceType.java`

### 第三阶段：联调前端

1. 类型管理页面接入 `/api/data-source/types`
2. 数据源新增/编辑页面改为读取启用类型列表
3. 提交数据源时传 `typeCode`
4. 列表/详情页改为展示 `typeName`

---

## 14. 建议输出给前端或调用方的接口语义

### 14.1 类型管理页面

类型列表建议返回：
- `id`
- `code`
- `name`
- `description`
- `enabled`
- `sortOrder`
- `createdAt`
- `updatedAt`

### 14.2 数据源新增/编辑页面

建议读取启用类型下拉接口：
- `GET /api/data-source/types/enabled`

前端下拉显示：
- `name`

前端提交值：
- `code`

### 14.3 数据源详情/列表页

建议直接返回：
- `typeCode`
- `typeName`

这样前端无需自己维护 code -> name 映射。

---

## 15. 验证方案

### 15.1 启动本地环境

可继续使用：
- `microservice/scripts/local-dev/start-local.ps1`

### 15.2 验证类型 CRUD

建议验证以下场景：

1. 新增一个类型，确认成功
2. 新增重复 `code`，确认返回 409
3. 新增重复 `name`，确认返回 409
4. 修改类型信息，确认成功
5. 查询列表与详情，确认返回正常
6. 删除未被引用类型，确认成功
7. 删除被引用类型，确认返回 409

### 15.3 验证数据源联动

在第二阶段完成后验证：

1. 创建数据源时传已存在 `typeCode`，确认成功
2. 创建数据源时传不存在 `typeCode`，确认失败
3. 禁用某个类型后，再用该类型创建数据源，确认是否符合设计预期
4. 查询数据源列表/详情，确认 `typeCode` / `typeName` 返回正确

### 15.4 验证异常返回格式

确认接口仍统一遵循 `ApiResponse<T>`：
- 参数校验失败 -> 400
- 重复数据 -> 409
- 资源不存在 -> 404
- 非法参数 -> 400

### 15.5 验证日志与健康检查

参考：
- 日志目录：`microservice/logs`
- 健康检查脚本：`microservice/scripts/local-dev/health-check.ps1`

---

## 16. 关键参考文件

### 16.1 现有 CRUD 参考

- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/DataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/controller/InternalDataSourceController.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceService.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/service/DataSourceServiceImpl.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/repository/DataSourceRepository.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceInfo.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/entity/DataSourceType.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceRequest.java`
- `microservice/service/data-source/src/main/java/com/governance/datasource/dto/DataSourceResponse.java`
- `microservice/service/data-source/src/main/resources/schema.sql`

### 16.2 异常处理参考

- `microservice/service/data-source/src/main/java/com/governance/datasource/exception/GlobalExceptionHandler.java`

---

## 17. 最终结论

本次需求的合理实现方式不是继续扩展 `DataSourceType` 枚举，而是：

1. 新增独立字典表 `data_source_types`
2. 先完成类型字典 CRUD
3. 再将 `data_sources` 的类型字段从 enum 平滑迁移到字典 code
4. 在数据源创建/更新时增加类型存在性校验
5. 删除类型时增加引用校验

这种方式能够在保持当前项目技术栈和代码风格一致的前提下，为后续新增更多数据源类型提供可扩展能力。
