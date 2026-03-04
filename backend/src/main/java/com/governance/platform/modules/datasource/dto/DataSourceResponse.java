package com.governance.platform.modules.datasource.dto;

import com.governance.platform.modules.datasource.entity.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceResponse {
    private Long id;
    private String name;
    private DataSourceType type;
    private String connectionUrl;
    private String username;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



