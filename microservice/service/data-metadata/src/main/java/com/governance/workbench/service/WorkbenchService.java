package com.governance.workbench.service;

import com.governance.workbench.dto.WorkbenchOverviewResponse;

/**
 * 工作台服务接口。
 *
 * <p>用于聚合多个领域服务的数据，向前端返回首页概览统计结果。</p>
 */
public interface WorkbenchService {

    /**
     * 获取工作台概览信息。
     *
     * @return 工作台概览
     */
    WorkbenchOverviewResponse getOverview();
}
