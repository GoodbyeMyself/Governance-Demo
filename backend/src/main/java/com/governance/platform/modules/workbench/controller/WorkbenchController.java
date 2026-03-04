package com.governance.platform.modules.workbench.controller;

import com.governance.platform.modules.workbench.dto.WorkbenchOverviewResponse;
import com.governance.platform.modules.workbench.service.WorkbenchService;
import com.governance.platform.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/overview")
    public ApiResponse<WorkbenchOverviewResponse> getOverview() {
        WorkbenchOverviewResponse response = workbenchService.getOverview();
        return ApiResponse.success("Success", response);
    }
}
