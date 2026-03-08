package com.governance.authcenter.verification;

import com.governance.authcenter.dto.AuthCenterSendEmailCodeResponse;

/**
 * 邮箱验证码服务接口。
 */
public interface EmailVerificationCodeService {

    /**
     * 发送邮箱验证码。
     *
     * @param scene 验证码场景
     * @param email 邮箱地址
     * @return 发送结果
     */
    AuthCenterSendEmailCodeResponse sendCode(EmailVerificationScene scene, String email);

    /**
     * 校验邮箱验证码。
     *
     * @param scene 验证码场景
     * @param email 邮箱地址
     * @param code  邮箱验证码
     */
    void validateCode(EmailVerificationScene scene, String email, String code);
}
