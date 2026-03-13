package com.governance.iotdevice.controller;

import com.governance.iotdevice.dto.IotDeviceResponse;
import com.governance.iotdevice.service.IotDeviceService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IoT 设备内部接口。
 */
@RestController
@RequestMapping("/internal/iot-devices")
@RequiredArgsConstructor
@Hidden
public class InternalIotDeviceController {

    private final IotDeviceService iotDeviceService;

    @GetMapping("/{id}")
    public IotDeviceResponse getById(@PathVariable Long id) {
        return iotDeviceService.getDeviceById(id);
    }

    @GetMapping
    public List<IotDeviceResponse> getAll() {
        return iotDeviceService.getAllDevices();
    }
}
