package com.governance.bms.user.service;

import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserProfileResponse;

import java.util.List;

public interface UserManagementService {
    UserProfileResponse register(UserRegisterRequest request);

    UserCredentialResponse getByUsername(String username);

    List<UserProfileResponse> listUsers();

    void markLastLogin(Long userId);

    UserProfileResponse updateUserRole(Long userId, UserRoleUpdateRequest request);

    List<String> listRoles();

    List<String> listPermissions();
}

