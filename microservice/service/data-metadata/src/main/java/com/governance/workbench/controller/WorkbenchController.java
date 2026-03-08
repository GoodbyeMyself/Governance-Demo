package com.governance.workbench.controller;

import com.governance.workbench.dto.WorkbenchOverviewResponse;
import com.governance.workbench.service.WorkbenchService;
import com.governance.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台对外接口。
 *
 * <p>负责向前端首页提供聚合概览数据。</p>
 */
@RestController
@RequestMapping("/api/data-metadata/workbench")
@RequiredArgsConstructor
@Tag(name = "Workbench", description = "Workbench statistics APIs")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    /**
     * 获取工作台概览。
     *
     * @return 概览统计数据
     */
    @GetMapping("/overview")
    @Operation(summary = "Get overview", description = "Return overview statistics")
    public ApiResponse<WorkbenchOverviewResponse> getOverview() {
        WorkbenchOverviewResponse response = workbenchService.getOverview();
        return ApiResponse.success("Success", response);
    }
}
