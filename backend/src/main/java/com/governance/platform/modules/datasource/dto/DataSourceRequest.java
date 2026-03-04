package com.governance.platform.modules.datasource.dto;

import com.governance.platform.modules.datasource.entity.DataSourceType;
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
public class DataSourceRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @NotNull(message = "type is required")
    private DataSourceType type;

    @Size(max = 500, message = "connectionUrl must be at most 500 characters")
    private String connectionUrl;

    @Size(max = 100, message = "username must be at most 100 characters")
    private String username;

    @Size(max = 100, message = "password must be at most 100 characters")
    private String password;

    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;
}



