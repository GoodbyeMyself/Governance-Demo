package com.governance.authcenter.notification;

import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.authcenter.verification.EmailVerificationScene;
import com.governance.shared.i18n.MessageResolver;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 验证码邮件发送服务实现。
 *
 * <p>支持真实 SMTP 发送和本地 mock 模式：
 * 若未配置 SMTP 或显式启用 mock，则只记录日志并可返回调试验证码。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationMailServiceImpl implements VerificationMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MessageResolver messageResolver;

    @Value("${auth-center.mail.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${auth-center.mail.debug-return-code:true}")
    private boolean debugReturnCode;

    @Value("${auth-center.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * 发送验证码邮件。
     *
     * @param scene         验证码场景
     * @param email         邮箱地址
     * @param code          验证码
     * @param expireSeconds 验证码过期秒数
     * @return 调试验证码，仅在 mock 模式下可能返回
     */
    @Override
    public String sendVerificationCode(
            EmailVerificationScene scene,
            String email,
            String code,
            long expireSeconds
    ) {
        if (mockEnabled) {
            log.info("Mock email verification code, scene={}, email={}, code={}", scene, email, code);
            return debugReturnCode ? code : null;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailHost)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.mail.senderNotConfigured"));
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(resolveMailFrom());
            helper.setTo(email);
            helper.setSubject(buildSubject(scene));
            helper.setText(buildContent(scene, code, expireSeconds), false);
            mailSender.send(message);
            return null;
        } catch (Exception ex) {
            log.error("Failed to send verification email, scene={}, email={}", scene, email, ex);
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.mail.sendFailed"));
        }
    }

    /**
     * 解析发件人邮箱地址。
     *
     * @return 发件人邮箱
     */
    private String resolveMailFrom() {
        String resolvedMailFrom = StringUtils.hasText(mailFrom)
                ? mailFrom.trim()
                : (StringUtils.hasText(mailUsername) ? mailUsername.trim() : null);
        if (!StringUtils.hasText(resolvedMailFrom)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.mail.fromNotConfigured"));
        }
        return resolvedMailFrom;
    }

    /**
     * 构建邮件标题。
     *
     * @param scene 验证码场景
     * @return 邮件标题
     */
    private String buildSubject(EmailVerificationScene scene) {
        return switch (scene) {
            case REGISTER -> messageResolver.getMessage("auth.mail.subject.register");
            case RESET_PASSWORD -> messageResolver.getMessage("auth.mail.subject.resetPassword");
        };
    }

    /**
     * 构建邮件正文。
     *
     * @param scene         验证码场景
     * @param code          验证码
     * @param expireSeconds 验证码过期秒数
     * @return 邮件正文
     */
    private String buildContent(EmailVerificationScene scene, String code, long expireSeconds) {
        String action = switch (scene) {
            case REGISTER -> messageResolver.getMessage("auth.mail.action.register");
            case RESET_PASSWORD -> messageResolver.getMessage("auth.mail.action.resetPassword");
        };
        long expireMinutes = Math.max(1L, (long) Math.ceil(expireSeconds / 60.0d));
        return messageResolver.getMessage("auth.mail.content", action, code, expireMinutes);
    }
}
