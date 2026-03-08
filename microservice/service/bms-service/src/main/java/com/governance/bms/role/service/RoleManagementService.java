package com.governance.bms.role.service;

import com.governance.bms.role.dto.RoleDefinitionResponse;
import com.governance.bms.role.dto.RoleDefinitionUpdateRequest;

import java.util.List;

/**
 * 角色定义管理服务接口。
 */
public interface RoleManagementService {

    /**
     * 查询全部角色定义。
     *
     * @return 角色定义列表
     */
    List<RoleDefinitionResponse> listRoleDefinitions();

    /**
     * 更新指定角色定义。
     *
     * @param roleCode 角色编码
     * @param request  更新请求
     * @return 更新后的角色定义
     */
    RoleDefinitionResponse updateRoleDefinition(
            String roleCode,
            RoleDefinitionUpdateRequest request
    );
}
