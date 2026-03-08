package com.governance.authcenter.verification;

import com.governance.authcenter.dto.AuthCenterSendEmailCodeResponse;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.authcenter.notification.VerificationMailService;
import com.governance.shared.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的邮箱验证码服务实现。
 *
 * <p>当前演示系统为单容器/单实例部署，
 * 因此使用内存缓存即可满足注册和找回密码场景。</p>
 */
@Service
@RequiredArgsConstructor
public class InMemoryEmailVerificationCodeService implements EmailVerificationCodeService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, EmailCodeEntry> codeStore = new ConcurrentHashMap<>();
    private final VerificationMailService verificationMailService;
    private final MessageResolver messageResolver;

    @Value("${auth-center.email-code-expire-seconds:300}")
    private long emailCodeExpireSeconds;

    @Value("${auth-center.email-code-resend-seconds:60}")
    private long emailCodeResendSeconds;

    /**
     * 发送邮箱验证码。
     *
     * @param scene 验证码场景
     * @param email 邮箱地址
     * @return 发送结果
     */
    @Override
    public AuthCenterSendEmailCodeResponse sendCode(EmailVerificationScene scene, String email) {
        clearExpiredCodes();

        if (scene == null || !StringUtils.hasText(email)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.emailCode.request.invalid"));
        }

        String normalizedEmail = email.trim().toLowerCase();
        String cacheKey = buildCacheKey(scene, normalizedEmail);
        EmailCodeEntry existing = codeStore.get(cacheKey);
        Instant now = Instant.now();
        if (existing != null && existing.nextSendAt().isAfter(now)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.emailCode.resend.tooFast"));
        }

        String code = randomCode();
        Instant expiresAt = now.plusSeconds(emailCodeExpireSeconds);
        Instant nextSendAt = now.plusSeconds(emailCodeResendSeconds);
        String debugCode = verificationMailService.sendVerificationCode(
                scene,
                normalizedEmail,
                code,
                emailCodeExpireSeconds
        );
        codeStore.put(cacheKey, new EmailCodeEntry(code, expiresAt, nextSendAt));
        return AuthCenterSendEmailCodeResponse.builder()
                .expiresIn(emailCodeExpireSeconds)
                .resendIn(emailCodeResendSeconds)
                .debugCode(debugCode)
                .build();
    }

    /**
     * 校验邮箱验证码。
     *
     * @param scene 验证码场景
     * @param email 邮箱地址
     * @param code  邮箱验证码
     */
    @Override
    public void validateCode(EmailVerificationScene scene, String email, String code) {
        clearExpiredCodes();

        if (scene == null || !StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.emailCode.invalid"));
        }

        String cacheKey = buildCacheKey(scene, email.trim().toLowerCase());
        EmailCodeEntry entry = codeStore.get(cacheKey);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            codeStore.remove(cacheKey);
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.emailCode.invalid"));
        }

        if (!entry.code().equalsIgnoreCase(code.trim())) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.emailCode.invalid"));
        }

        codeStore.remove(cacheKey);
    }

    /**
     * 构建缓存键。
     *
     * @param scene 验证码场景
     * @param email 邮箱
     * @return 缓存键
     */
    private String buildCacheKey(EmailVerificationScene scene, String email) {
        return scene.name() + ":" + email;
    }

    /**
     * 生成 6 位数字验证码。
     *
     * @return 验证码
     */
    private String randomCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    /**
     * 清理过期验证码。
     */
    private void clearExpiredCodes() {
        Instant now = Instant.now();
        codeStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    /**
     * 邮箱验证码缓存条目。
     *
     * @param code       验证码
     * @param expiresAt  过期时间
     * @param nextSendAt 下次允许发送时间
     */
    private record EmailCodeEntry(String code, Instant expiresAt, Instant nextSendAt) {
    }
}
