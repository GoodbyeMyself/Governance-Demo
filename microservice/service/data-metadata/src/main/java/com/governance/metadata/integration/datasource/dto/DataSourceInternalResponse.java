package com.governance.metadata.integration.datasource.dto;

import com.governance.datasource.entity.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInternalResponse {
    private Long id;
    private String name;
    private DataSourceType type;
    private String connectionUrl;
    private String username;
    private String description;
}
