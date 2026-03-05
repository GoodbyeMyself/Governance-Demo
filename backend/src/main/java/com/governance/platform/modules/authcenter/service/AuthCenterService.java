package com.governance.platform.modules.authcenter.service;

import com.governance.platform.modules.authcenter.dto.AuthCenterLoginRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterLoginResponse;
import com.governance.platform.modules.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUpdateUserRoleRequest;
import com.governance.platform.modules.authcenter.dto.AuthCenterUserProfileResponse;

import java.util.List;

public interface AuthCenterService {
    AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request);

    AuthCenterLoginResponse login(AuthCenterLoginRequest request);

    AuthCenterUserProfileResponse me();

    void logout();

    List<AuthCenterUserProfileResponse> listUsers();

    AuthCenterUserProfileResponse updateUserRole(Long userId, AuthCenterUpdateUserRoleRequest request);
}
