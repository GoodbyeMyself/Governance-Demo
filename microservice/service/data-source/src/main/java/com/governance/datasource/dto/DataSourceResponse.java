package com.governance.datasource.dto;

import com.governance.datasource.entity.DataSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据源响应结果")
public class DataSourceResponse {
    @Schema(description = "数据源ID", example = "1")
    private Long id;

    @Schema(description = "数据源名称", example = "生产库-订单中心")
    private String name;

    @Schema(description = "数据源类型", allowableValues = {"DATABASE", "FILE_SYSTEM"}, example = "DATABASE")
    private DataSourceType type;

    @Schema(description = "连接地址")
    private String connectionUrl;

    @Schema(description = "连接用户名")
    private String username;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}



