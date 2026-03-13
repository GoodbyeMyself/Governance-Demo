package com.governance.iotcollection.controller;

import com.governance.iotcollection.dto.IotTelemetryHistoryItemResponse;
import com.governance.iotcollection.dto.IotTelemetryLatestItemResponse;
import com.governance.iotcollection.dto.IotTelemetryOverviewResponse;
import com.governance.iotcollection.service.IotTelemetryService;
import com.governance.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 实时数据对外接口。
 */
@RestController
@RequestMapping("/api/iot-collection/telemetry")
@RequiredArgsConstructor
@Tag(name = "IoT Telemetry", description = "IoT telemetry query APIs")
public class IotTelemetryController {

    private final IotTelemetryService iotTelemetryService;

    @GetMapping("/latest/{deviceId}")
    @Operation(summary = "Get latest telemetry", description = "Query latest telemetry by device ID")
    public ApiResponse<List<IotTelemetryLatestItemResponse>> getLatestTelemetry(@PathVariable Long deviceId) {
        return ApiResponse.success("Success", iotTelemetryService.getLatestTelemetryByDeviceId(deviceId));
    }

    @GetMapping("/history")
    @Operation(summary = "Get telemetry history", description = "Query telemetry history by device and time range")
    public ApiResponse<List<IotTelemetryHistoryItemResponse>> getTelemetryHistory(
            @RequestParam Long deviceId,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ApiResponse.success(
                "Success",
                iotTelemetryService.getTelemetryHistory(deviceId, metricCode, startTime, endTime)
        );
    }

    @GetMapping("/overview")
    @Operation(summary = "Get telemetry overview", description = "Query telemetry overview and trend")
    public ApiResponse<IotTelemetryOverviewResponse> getTelemetryOverview() {
        return ApiResponse.success("Success", iotTelemetryService.getTelemetryOverview());
    }
}
