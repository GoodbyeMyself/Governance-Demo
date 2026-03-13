package com.governance.iotdevice.exception;

/**
 * IoT 设备被占用异常。
 */
public class IotDeviceInUseException extends RuntimeException {

    public IotDeviceInUseException(String message) {
        super(message);
    }
}
