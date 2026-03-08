package com.governance.metadata.controller;

import com.governance.metadata.repository.MetadataCollectionTaskRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/metadata-tasks")
@RequiredArgsConstructor
@Hidden
public class InternalMetadataController {

    private final MetadataCollectionTaskRepository metadataCollectionTaskRepository;

    @GetMapping("/count-by-data-source/{id}")
    public Long countByDataSourceId(@PathVariable("id") Long dataSourceId) {
        return metadataCollectionTaskRepository.countByDataSourceId(dataSourceId);
    }
}
