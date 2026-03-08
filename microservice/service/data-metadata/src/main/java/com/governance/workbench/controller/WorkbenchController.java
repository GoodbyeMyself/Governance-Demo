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

@RestController
@RequestMapping("/api/data-metadata/workbench")
@RequiredArgsConstructor
@Tag(name = "Workbench", description = "Workbench statistics APIs")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @GetMapping("/overview")
    @Operation(summary = "Get overview", description = "Return overview statistics")
    public ApiResponse<WorkbenchOverviewResponse> getOverview() {
        WorkbenchOverviewResponse response = workbenchService.getOverview();
        return ApiResponse.success("Success", response);
    }
}
