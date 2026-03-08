package com.governance.metadata.integration.datasource.dto;

import com.governance.datasource.entity.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源服务内部响应对象。
 * <p>
 * 元数据服务通过内部接口调用数据源服务时，
 * 使用该对象接收数据源基础连接信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInternalResponse {

    /**
     * 数据源 ID。
     */
    private Long id;

    /**
     * 数据源名称。
     */
    private String name;

    /**
     * 数据源类型。
     */
    private DataSourceType type;

    /**
     * 连接地址。
     */
    private String connectionUrl;

    /**
     * 连接用户名。
     */
    private String username;

    /**
     * 描述信息。
     */
    private String description;
}
