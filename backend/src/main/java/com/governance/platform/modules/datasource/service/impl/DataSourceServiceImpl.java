package com.governance.platform.modules.datasource.service.impl;

import com.governance.platform.modules.datasource.dto.DataSourceRequest;
import com.governance.platform.modules.datasource.dto.DataSourceResponse;
import com.governance.platform.modules.datasource.entity.DataSourceInfo;
import com.governance.platform.modules.datasource.exception.DataSourceInUseException;
import com.governance.platform.modules.datasource.exception.DuplicateDataSourceException;
import com.governance.platform.shared.exception.ResourceNotFoundException;
import com.governance.platform.modules.datasource.repository.DataSourceRepository;
import com.governance.platform.modules.metadata.repository.MetadataCollectionTaskRepository;
import com.governance.platform.modules.datasource.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    @Override
    public DataSourceResponse createDataSource(DataSourceRequest request) {
        if (dataSourceRepository.existsByName(request.getName())) {
            throw new DuplicateDataSourceException("Data source name already exists: " + request.getName());
        }

        DataSourceInfo entity = DataSourceInfo.builder()
                .name(request.getName())
                .type(request.getType())
                .connectionUrl(request.getConnectionUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .description(request.getDescription())
                .build();

        DataSourceInfo saved = dataSourceRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public void deleteDataSource(Long id) {
        DataSourceInfo existing = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));

        long referenceCount = metadataCollectionTaskRepository.countByDataSource_Id(id);
        if (referenceCount > 0) {
            throw new DataSourceInUseException(
                    "Data source is referenced by metadata collection tasks: " + id
            );
        }

        dataSourceRepository.delete(existing);
    }

    @Override
    public DataSourceResponse updateDataSource(Long id, DataSourceRequest request) {
        DataSourceInfo existing = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found: " + id));

        if (dataSourceRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateDataSourceException("Data source name already exists: " + request.getName());
        }

        existing.setName(request.getName());
        existing.setType(request.getType());
        existing.setConnectionUrl(request.getConnectionUrl());
        existing.setUsername(request.getUsername());
        existing.setPassword(request.getPassword());
        existing.setDescription(request.getDescription());

        DataSourceInfo updated = dataSourceRepository.save(existing);
        return toResponse(updated);
    }

    @Override
    public List<DataSourceResponse> getAllDataSources() {
        return dataSourceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DataSourceResponse toResponse(DataSourceInfo entity) {
        return DataSourceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .connectionUrl(entity.getConnectionUrl())
                .username(entity.getUsername())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}



