package com.governance.iotdevice.controller;

import com.governance.iotdevice.dto.IotDeviceRequest;
import com.governance.iotdevice.dto.IotDeviceResponse;
import com.governance.iotdevice.service.IotDeviceService;
import com.governance.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IoT 设备对外接口。
 */
@RestController
@RequestMapping("/api/iot-device")
@RequiredArgsConstructor
@Tag(name = "IoT Device", description = "IoT device CRUD APIs")
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;

    @PostMapping
    @Operation(summary = "Create IoT device", description = "Create a new IoT device")
    public ApiResponse<IotDeviceResponse> createDevice(@Valid @RequestBody IotDeviceRequest request) {
        return ApiResponse.success("IoT device created", iotDeviceService.createDevice(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update IoT device", description = "Update an IoT device by ID")
    public ApiResponse<IotDeviceResponse> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody IotDeviceRequest request
    ) {
        return ApiResponse.success("IoT device updated", iotDeviceService.updateDevice(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete IoT device", description = "Delete an IoT device by ID")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        iotDeviceService.deleteDevice(id);
        return ApiResponse.success("IoT device deleted", null);
    }

    @GetMapping
    @Operation(summary = "List IoT devices", description = "Query all IoT devices")
    public ApiResponse<List<IotDeviceResponse>> getAllDevices() {
        return ApiResponse.success("Success", iotDeviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get IoT device details", description = "Query an IoT device by ID")
    public ApiResponse<IotDeviceResponse> getDeviceById(@PathVariable Long id) {
        return ApiResponse.success("Success", iotDeviceService.getDeviceById(id));
    }
}
