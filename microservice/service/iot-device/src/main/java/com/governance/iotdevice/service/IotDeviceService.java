package com.governance.iotdevice.service;

import com.governance.iotdevice.dto.IotDeviceRequest;
import com.governance.iotdevice.dto.IotDeviceResponse;

import java.util.List;

/**
 * IoT 设备领域服务接口。
 */
public interface IotDeviceService {

    IotDeviceResponse createDevice(IotDeviceRequest request);

    IotDeviceResponse updateDevice(Long id, IotDeviceRequest request);

    void deleteDevice(Long id);

    List<IotDeviceResponse> getAllDevices();

    IotDeviceResponse getDeviceById(Long id);
}
