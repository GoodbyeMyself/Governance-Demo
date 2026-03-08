package com.governance.authcenter.service;

import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;

public interface AuthCenterService {
    AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request);

    AuthCenterLoginResponse login(AuthCenterLoginRequest request);

    AuthCenterUserProfileResponse me();

    void logout();
}
