package com.governance.authcenter.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 认证中心 Cookie 写入服务。
 *
 * <p>负责统一生成和清理承载 JWT 的 HttpOnly Cookie，
 * 避免控制器散落重复的 Set-Cookie 逻辑。</p>
 */
@Component
public class AuthCenterCookieService {

    @Value("${auth-center.cookie.name:governance_access_token}")
    private String cookieName;

    @Value("${auth-center.cookie.path:/}")
    private String cookiePath;

    @Value("${auth-center.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${auth-center.cookie.same-site:Lax}")
    private String sameSite;

    /**
     * 写入认证 Cookie。
     *
     * @param response      当前响应
     * @param token         JWT
     * @param expireSeconds 有效期，单位秒
     */
    public void writeAuthCookie(
            HttpServletResponse response,
            String token,
            Long expireSeconds
    ) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path(cookiePath);

        if (expireSeconds != null && expireSeconds > 0) {
            builder.maxAge(Duration.ofSeconds(expireSeconds));
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    /**
     * 清理认证 Cookie。
     *
     * @param response 当前响应
     */
    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path(cookiePath)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
