package com.governance.iotdevice.integration.collection;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * IoT 采集服务内部调用客户端。
 */
@FeignClient(
        name = "${integration.collection-service.name:iot-collection}",
        path = "${integration.collection-service.path:/internal/iot-collection/tasks}"
)
public interface IotCollectionTaskClient {

    @GetMapping("/count-by-device/{id}")
    Long countByDeviceId(@PathVariable("id") Long deviceId);
}
