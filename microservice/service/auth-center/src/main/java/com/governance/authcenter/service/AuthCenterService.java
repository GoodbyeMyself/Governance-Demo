package com.governance.authcenter.service;

import com.governance.authcenter.dto.AuthCenterCaptchaResponse;
import com.governance.authcenter.dto.AuthCenterLoginRequest;
import com.governance.authcenter.dto.AuthCenterLoginResponse;
import com.governance.authcenter.dto.AuthCenterProfileUpdateRequest;
import com.governance.authcenter.dto.AuthCenterRegisterRequest;
import com.governance.authcenter.dto.AuthCenterResetPasswordRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeRequest;
import com.governance.authcenter.dto.AuthCenterSendEmailCodeResponse;
import com.governance.authcenter.dto.AuthCenterUserProfileResponse;

/**
 * 认证中心领域服务接口。
 *
 * <p>定义验证码、注册、登录、找回密码、当前用户查询与退出登录等认证域能力。</p>
 */
public interface AuthCenterService {

    /**
     * 生成图形验证码。
     *
     * @return 图形验证码响应
     */
    AuthCenterCaptchaResponse getCaptcha();

    /**
     * 发送邮箱验证码。
     *
     * @param request 发送请求
     * @return 发送结果
     */
    AuthCenterSendEmailCodeResponse sendEmailCode(AuthCenterSendEmailCodeRequest request);

    /**
     * 注册平台账号。
     *
     * @param request 注册请求
     * @return 注册后的用户资料
     */
    AuthCenterUserProfileResponse register(AuthCenterRegisterRequest request);

    /**
     * 执行登录并签发 JWT。
     *
     * @param request 登录请求
     * @return 登录结果与令牌
     */
    AuthCenterLoginResponse login(AuthCenterLoginRequest request);

    /**
     * 重置用户密码。
     *
     * @param request 重置密码请求
     */
    void resetPassword(AuthCenterResetPasswordRequest request);

    /**
     * 获取当前登录用户资料。
     *
     * @return 当前用户资料
     */
    AuthCenterUserProfileResponse me();

    /**
     * 更新当前登录用户资料。
     *
     * @param request 资料更新请求
     * @return 新的登录态信息
     */
    AuthCenterLoginResponse updateCurrentUserProfile(AuthCenterProfileUpdateRequest request);

    /**
     * 执行退出登录。
     *
     * <p>当前系统采用无状态 JWT，退出动作主要用于统一语义，不涉及服务端会话清理。</p>
     */
    void logout();
}
