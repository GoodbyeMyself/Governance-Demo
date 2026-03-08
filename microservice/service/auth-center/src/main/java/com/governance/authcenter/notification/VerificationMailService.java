package com.governance.authcenter.notification;

import com.governance.authcenter.verification.EmailVerificationScene;

/**
 * 验证码邮件发送服务接口。
 */
public interface VerificationMailService {

    /**
     * 发送验证码邮件。
     *
     * @param scene         验证码场景
     * @param email         邮箱地址
     * @param code          验证码
     * @param expireSeconds 验证码过期秒数
     * @return 调试验证码，仅在 mock 模式下可能返回
     */
    String sendVerificationCode(
            EmailVerificationScene scene,
            String email,
            String code,
            long expireSeconds
    );
}
