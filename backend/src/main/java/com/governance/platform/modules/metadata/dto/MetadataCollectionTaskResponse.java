package com.governance.platform.modules.metadata.dto;

import com.governance.platform.modules.datasource.entity.DataSourceType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScheduleType;
import com.governance.platform.modules.metadata.entity.MetadataCollectionScope;
import com.governance.platform.modules.metadata.entity.MetadataCollectionStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataCollectionTaskResponse {
    private Long id;
    private String taskName;
    private Long dataSourceId;
    private String dataSourceName;
    private DataSourceType dataSourceType;
    private MetadataCollectionStrategy strategy;
    private MetadataCollectionScope scope;
    private String targetPattern;
    private MetadataCollectionScheduleType scheduleType;
    private String cronExpression;
    private String configJson;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



