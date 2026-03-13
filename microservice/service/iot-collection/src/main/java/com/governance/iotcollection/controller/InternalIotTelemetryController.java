package com.governance.iotcollection.controller;

import com.governance.iotcollection.dto.IotTelemetryOverviewResponse;
import com.governance.iotcollection.service.IotTelemetryService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT 实时数据内部接口。
 */
@RestController
@RequestMapping("/internal/iot-collection/telemetry")
@RequiredArgsConstructor
@Hidden
public class InternalIotTelemetryController {

    private final IotTelemetryService iotTelemetryService;

    @GetMapping("/stats/overview")
    public IotTelemetryOverviewResponse getTelemetryOverview() {
        return iotTelemetryService.getTelemetryOverview();
    }
}
