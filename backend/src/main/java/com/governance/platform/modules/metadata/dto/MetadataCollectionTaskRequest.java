package com.governance.platform.modules.metadata.dto;

import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScope;
import com.governance.platform.modules.metadata.entity.MetadataCollectionStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataCollectionTaskRequest {

    @NotBlank(message = "taskName is required")
    @Size(max = 100, message = "taskName must be at most 100 characters")
    private String taskName;

    @NotNull(message = "dataSourceId is required")
    @Positive(message = "dataSourceId must be positive")
    private Long dataSourceId;

    @NotNull(message = "strategy is required")
    private MetadataCollectionStrategy strategy;

    @NotNull(message = "scope is required")
    private MetadataCollectionScope scope;

    @Size(max = 500, message = "targetPattern must be at most 500 characters")
    private String targetPattern;

    @NotNull(message = "scheduleType is required")
    private MetadataCollectionScheduleType scheduleType;

    @Size(max = 100, message = "cronExpression must be at most 100 characters")
    private String cronExpression;

    @Size(max = 2000, message = "configJson must be at most 2000 characters")
    private String configJson;

    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}



