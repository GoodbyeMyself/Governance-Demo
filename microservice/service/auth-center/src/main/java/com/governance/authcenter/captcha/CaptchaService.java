package com.governance.authcenter.captcha;

import com.governance.authcenter.dto.AuthCenterCaptchaResponse;

/**
 * 图形验证码服务接口。
 */
public interface CaptchaService {

    /**
     * 生成新的图形验证码。
     *
     * @return 图形验证码响应
     */
    AuthCenterCaptchaResponse generateCaptcha();

    /**
     * 校验图形验证码。
     *
     * @param captchaId   验证码 ID
     * @param captchaCode 验证码内容
     */
    void validateCaptcha(String captchaId, String captchaCode);
}
