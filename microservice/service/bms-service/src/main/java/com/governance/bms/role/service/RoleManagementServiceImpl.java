package com.governance.bms.role.service;

import com.governance.bms.role.dto.RoleDefinitionResponse;
import com.governance.bms.role.dto.RoleDefinitionUpdateRequest;
import com.governance.bms.role.entity.BmsRole;
import com.governance.bms.role.exception.RoleOperationException;
import com.governance.bms.role.repository.BmsRoleRepository;
import com.governance.bms.user.entity.UserRole;
import com.governance.bms.user.repository.BmsUserRepository;
import com.governance.shared.exception.ResourceNotFoundException;
import com.governance.shared.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 角色定义管理服务实现。
 *
 * <p>负责角色展示名称维护和角色使用情况统计。
 * 当前内置 {@code ADMIN} 角色为只读角色，不允许修改。</p>
 */
@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements RoleManagementService {

    private final BmsRoleRepository roleRepository;
    private final BmsUserRepository userRepository;
    private final MessageResolver messageResolver;

    /**
     * 查询全部角色定义。
     *
     * @return 角色定义列表
     */
    @Override
    public List<RoleDefinitionResponse> listRoleDefinitions() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 更新指定角色定义。
     *
     * @param roleCode 角色编码
     * @param request  更新请求
     * @return 更新后的角色定义
     */
    @Override
    @Transactional
    public RoleDefinitionResponse updateRoleDefinition(
            String roleCode,
            RoleDefinitionUpdateRequest request
    ) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        BmsRole role = roleRepository.findByRoleCode(normalizedRoleCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageResolver.getMessage("bms.role.notFound", normalizedRoleCode)
                ));

        if (UserRole.ADMIN.name().equals(normalizedRoleCode)) {
            throw new RoleOperationException(messageResolver.getMessage("bms.role.admin.readOnly"));
        }

        role.setRoleName(request.getRoleName().trim());
        return toResponse(roleRepository.save(role));
    }

    /**
     * 规范化角色编码。
     *
     * @param roleCode 原始角色编码
     * @return 规范化后的角色编码
     */
    private String normalizeRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new IllegalArgumentException(messageResolver.getMessage("bms.role.code.required"));
        }
        return roleCode.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 判断角色是否允许编辑。
     *
     * @param roleCode 角色编码
     * @return 是否可编辑
     */
    private boolean isEditable(String roleCode) {
        return !UserRole.ADMIN.name().equals(roleCode);
    }

    /**
     * 统计绑定当前角色的用户数量。
     *
     * @param roleCode 角色编码
     * @return 用户数量
     */
    private long countUsers(String roleCode) {
        try {
            return userRepository.countByRole(UserRole.valueOf(roleCode));
        } catch (IllegalArgumentException ex) {
            return 0L;
        }
    }

    /**
     * 把角色实体转换为对外响应。
     *
     * @param role 角色实体
     * @return 角色定义响应
     */
    private RoleDefinitionResponse toResponse(BmsRole role) {
        return RoleDefinitionResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .editable(isEditable(role.getRoleCode()))
                .userCount(countUsers(role.getRoleCode()))
                .build();
    }
}
