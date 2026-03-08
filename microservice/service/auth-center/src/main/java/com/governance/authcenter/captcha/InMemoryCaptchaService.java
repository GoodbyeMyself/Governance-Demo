package com.governance.authcenter.captcha;

import com.governance.authcenter.dto.AuthCenterCaptchaResponse;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.shared.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的图形验证码服务实现。
 *
 * <p>适用于当前演示系统的单实例运行模式，
 * 验证码会在校验成功后立即失效。</p>
 */
@Service
@RequiredArgsConstructor
public class InMemoryCaptchaService implements CaptchaService {

    private static final char[] CAPTCHA_CHARS =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private final MessageResolver messageResolver;

    @Value("${auth-center.captcha-expire-seconds:180}")
    private long captchaExpireSeconds;

    /**
     * 生成新的图形验证码。
     *
     * @return 图形验证码响应
     */
    @Override
    public AuthCenterCaptchaResponse generateCaptcha() {
        clearExpiredCaptchas();

        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String captchaCode = randomCode(4);
        captchaStore.put(
                captchaId,
                new CaptchaEntry(captchaCode, Instant.now().plusSeconds(captchaExpireSeconds))
        );

        return AuthCenterCaptchaResponse.builder()
                .captchaId(captchaId)
                .imageData(buildImageData(captchaCode))
                .expiresIn(captchaExpireSeconds)
                .build();
    }

    /**
     * 校验图形验证码。
     *
     * @param captchaId   验证码 ID
     * @param captchaCode 验证码内容
     */
    @Override
    public void validateCaptcha(String captchaId, String captchaCode) {
        clearExpiredCaptchas();

        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.captcha.invalid"));
        }

        CaptchaEntry entry = captchaStore.get(captchaId.trim());
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            captchaStore.remove(captchaId.trim());
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.captcha.invalid"));
        }

        if (!entry.code().equalsIgnoreCase(captchaCode.trim())) {
            throw new AuthCenterOperationException(messageResolver.getMessage("auth.captcha.invalid"));
        }

        captchaStore.remove(captchaId.trim());
    }

    /**
     * 清理已过期的验证码。
     */
    private void clearExpiredCaptchas() {
        Instant now = Instant.now();
        captchaStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    /**
     * 生成指定长度的随机验证码。
     *
     * @param length 长度
     * @return 验证码字符串
     */
    private String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(CAPTCHA_CHARS[random.nextInt(CAPTCHA_CHARS.length)]);
        }
        return builder.toString();
    }

    /**
     * 构建 SVG 格式验证码图片并转换为 Data URL。
     *
     * @param code 验证码内容
     * @return Data URL
     */
    private String buildImageData(String code) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='132' height='44' viewBox='0 0 132 44'>")
                .append("<defs><linearGradient id='bg' x1='0%' y1='0%' x2='100%' y2='100%'>")
                .append("<stop offset='0%' stop-color='#0f172a'/><stop offset='100%' stop-color='#1d4ed8'/>")
                .append("</linearGradient></defs>")
                .append("<rect width='132' height='44' rx='12' fill='url(#bg)'/>");

        for (int index = 0; index < 6; index++) {
            svg.append("<circle cx='")
                    .append(12 + random.nextInt(108))
                    .append("' cy='")
                    .append(8 + random.nextInt(28))
                    .append("' r='")
                    .append(2 + random.nextInt(4))
                    .append("' fill='rgba(255,255,255,0.18)'/>");
        }

        for (int index = 0; index < code.length(); index++) {
            int x = 18 + index * 24;
            int y = 28 + random.nextInt(7) - 3;
            int rotate = random.nextInt(25) - 12;
            svg.append("<text x='")
                    .append(x)
                    .append("' y='")
                    .append(y)
                    .append("' font-family='Arial' font-size='24' font-weight='700' fill='#f8fafc' ")
                    .append("transform='rotate(")
                    .append(rotate)
                    .append(" ")
                    .append(x)
                    .append(" ")
                    .append(y)
                    .append(")'>")
                    .append(code.charAt(index))
                    .append("</text>");
        }

        svg.append("</svg>");
        String base64 = Base64.getEncoder()
                .encodeToString(svg.toString().getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    /**
     * 内存验证码条目。
     *
     * @param code      验证码内容
     * @param expiresAt 过期时间
     */
    private record CaptchaEntry(String code, Instant expiresAt) {
    }
}
