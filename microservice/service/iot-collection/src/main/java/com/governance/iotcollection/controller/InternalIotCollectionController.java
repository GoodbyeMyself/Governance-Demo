package com.governance.iotcollection.controller;

import com.governance.iotcollection.repository.IotCollectionTaskRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT 采集任务内部接口。
 */
@RestController
@RequestMapping("/internal/iot-collection/tasks")
@RequiredArgsConstructor
@Hidden
public class InternalIotCollectionController {

    private final IotCollectionTaskRepository iotCollectionTaskRepository;

    @GetMapping("/count-by-device/{id}")
    public Long countByDeviceId(@PathVariable("id") Long deviceId) {
        return iotCollectionTaskRepository.countByDeviceId(deviceId);
    }
}
