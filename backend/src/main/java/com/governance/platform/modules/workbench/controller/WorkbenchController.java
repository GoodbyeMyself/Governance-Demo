package com.governance.platform.modules.workbench.controller;

import com.governance.platform.modules.workbench.dto.WorkbenchOverviewResponse;
import com.governance.platform.modules.workbench.service.WorkbenchService;
import com.governance.platform.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workbench")
@RequiredArgsConstructor
@Tag(name = "工作台", description = "工作台首页概览统计接口")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/overview")
    @Operation(summary = "获取工作台概览", description = "返回数据源、采集任务和趋势等统计信息")
    public ApiResponse<WorkbenchOverviewResponse> getOverview() {
        WorkbenchOverviewResponse response = workbenchService.getOverview();
        return ApiResponse.success("Success", response);
    }
}
