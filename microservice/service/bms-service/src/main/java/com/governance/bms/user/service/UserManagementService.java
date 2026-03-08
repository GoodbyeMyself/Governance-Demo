package com.governance.bms.user.service;

import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserPasswordResetRequest;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.dto.UserProfileUpdateRequest;
import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;

import java.util.List;

/**
 * 用户管理领域服务接口。
 *
 * <p>负责统一收口用户注册、凭据查询、资料维护、密码重置和角色调整等能力，
 * 让认证中心与后台管理端都通过同一套领域服务访问用户主数据。</p>
 */
public interface UserManagementService {

    /**
     * 注册平台用户。
     *
     * @param request 用户注册请求
     * @return 注册后的用户资料
     */
    UserProfileResponse register(UserRegisterRequest request);

    /**
     * 按用户名查询用户凭据。
     *
     * @param username 用户名
     * @return 用户凭据与基础资料
     */
    UserCredentialResponse getByUsername(String username);

    /**
     * 查询全部用户。
     *
     * @return 用户列表
     */
    List<UserProfileResponse> listUsers();

    /**
     * 标记指定用户最近登录时间。
     *
     * @param userId 用户 ID
     */
    void markLastLogin(Long userId);

    /**
     * 修改指定用户角色。
     *
     * @param userId 用户 ID
     * @param request 角色更新请求
     * @return 更新后的用户资料
     */
    UserProfileResponse updateUserRole(Long userId, UserRoleUpdateRequest request);

    /**
     * 按用户名更新用户资料。
     *
     * @param currentUsername 当前用户名
     * @param request         用户资料更新请求
     * @return 更新后的用户资料
     */
    UserProfileResponse updateUserProfileByUsername(
            String currentUsername,
            UserProfileUpdateRequest request
    );

    /**
     * 按用户名重置密码。
     *
     * @param username 用户名
     * @param request  密码重置请求
     */
    void resetPasswordByUsername(String username, UserPasswordResetRequest request);

    /**
     * 查询系统支持的角色编码列表。
     *
     * @return 角色编码列表
     */
    List<String> listRoles();

    /**
     * 查询系统支持的权限编码列表。
     *
     * @return 权限编码列表
     */
    List<String> listPermissions();
}
