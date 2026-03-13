package com.governance.iotcollection.integration.device;

import com.governance.iotcollection.integration.device.dto.IotDeviceInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * IoT 设备服务内部调用客户端。
 */
@FeignClient(
        name = "${integration.iot-device-service.name:iot-device}",
        path = "${integration.iot-device-service.path:/internal/iot-devices}"
)
public interface IotDeviceClient {

    @GetMapping("/{id}")
    IotDeviceInternalResponse getById(@PathVariable("id") Long id);
}
