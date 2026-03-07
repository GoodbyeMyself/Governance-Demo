package com.governance.platform.modules.datasource.dto;

import com.governance.platform.modules.datasource.entity.DataSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据源新增/更新请求参数")
public class DataSourceRequest {

    @Schema(description = "数据源名称", example = "生产库-订单中心")
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @Schema(description = "数据源类型", allowableValues = {"DATABASE", "FILE_SYSTEM"}, example = "DATABASE")
    @NotNull(message = "type is required")
    private DataSourceType type;

    @Schema(description = "连接地址（数据库 JDBC URL 或文件系统地址）", example = "jdbc:mysql://127.0.0.1:3306/demo")
    @Size(max = 500, message = "connectionUrl must be at most 500 characters")
    private String connectionUrl;

    @Schema(description = "连接用户名", example = "root")
    @Size(max = 100, message = "username must be at most 100 characters")
    private String username;

    @Schema(description = "连接密码", example = "your_password")
    @Size(max = 100, message = "password must be at most 100 characters")
    private String password;

    @Schema(description = "数据源描述", example = "用于同步订单域业务数据")
    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;
}



