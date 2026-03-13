package com.governance.iotdevice.repository;

import com.governance.iotdevice.entity.IotDeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * IoT 设备仓储接口。
 */
public interface IotDeviceRepository extends JpaRepository<IotDeviceInfo, Long> {

    boolean existsByDeviceCode(String deviceCode);

    boolean existsByDeviceCodeAndIdNot(String deviceCode, Long id);
}
