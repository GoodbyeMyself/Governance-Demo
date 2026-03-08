package com.governance.datasource.dto;

import com.governance.datasource.entity.DataSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据源响应对象。
 * <p>
 * 对外返回数据源基础信息及审计字段，
 * 供管理页面列表、详情和任务配置等场景复用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据源响应结果")
public class DataSourceResponse {

    /**
     * 数据源 ID。
     */
    @Schema(description = "数据源 ID", example = "1")
    private Long id;

    /**
     * 数据源名称。
     */
    @Schema(description = "数据源名称", example = "生产库-订单中心")
    private String name;

    /**
     * 数据源类型。
     */
    @Schema(description = "数据源类型", allowableValues = {"DATABASE", "FILE_SYSTEM"}, example = "DATABASE")
    private DataSourceType type;

    /**
     * 连接地址。
     */
    @Schema(description = "连接地址")
    private String connectionUrl;

    /**
     * 连接用户名。
     */
    @Schema(description = "连接用户名")
    private String username;

    /**
     * 数据源描述。
     */
    @Schema(description = "描述信息")
    private String description;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
