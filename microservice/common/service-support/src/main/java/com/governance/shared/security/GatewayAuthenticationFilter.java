package com.governance.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关透传鉴权过滤器。
 *
 * <p>业务服务并不直接解析 JWT，而是信任网关转发过来的认证头信息。
 * 该过滤器的职责就是把这些头信息转换为 Spring Security 可识别的认证对象。</p>
 */
@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_GATEWAY_AUTH = "X-Gateway-Auth";
    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_ROLES = "X-Auth-Roles";

    @Value("${security.gateway.trusted-token:change-me-gateway-token}")
    private String trustedGatewayToken;

    /**
     * 在请求进入控制器前建立认证上下文。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateByGatewayHeaders(request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断当前请求是否应跳过网关头鉴权。
     *
     * <p>登录、注册、内部接口、Swagger 与健康检查不依赖用户态，
     * 因此不需要再从网关头中恢复认证信息。</p>
     *
     * @param request 当前请求
     * @return 是否跳过
     */
    private boolean shouldSkip(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        return uri.startsWith("/internal/")
                || "/api/auth-center/captcha".equals(uri)
                || "/api/auth-center/login".equals(uri)
                || "/api/auth-center/register".equals(uri)
                || "/api/auth-center/email-codes/send".equals(uri)
                || "/api/auth-center/password/reset".equals(uri)
                || "/swagger-ui.html".equals(uri)
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs")
                || "/actuator/health".equals(uri);
    }

    /**
     * 根据网关透传的请求头建立认证对象。
     *
     * <p>只有当内部信任令牌正确、且能拿到用户名时，才会把认证写入安全上下文。</p>
     *
     * @param request 当前请求
     */
    private void authenticateByGatewayHeaders(HttpServletRequest request) {
        String gatewayToken = request.getHeader(HEADER_GATEWAY_AUTH);
        if (!StringUtils.hasText(gatewayToken) || !trustedGatewayToken.equals(gatewayToken.trim())) {
            return;
        }

        String username = request.getHeader(HEADER_AUTH_USER);
        if (!StringUtils.hasText(username)) {
            return;
        }

        List<SimpleGrantedAuthority> authorities = parseAuthorities(request.getHeader(HEADER_AUTH_ROLES));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username.trim(), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 解析角色头并转换为 Spring Security 权限集合。
     *
     * <p>如果头信息为空，则默认兜底为 `ROLE_USER`，保证最小权限场景仍可工作。</p>
     *
     * @param roleHeader 网关透传的角色头
     * @return 权限列表
     */
    private List<SimpleGrantedAuthority> parseAuthorities(String roleHeader) {
        if (!StringUtils.hasText(roleHeader)) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        List<SimpleGrantedAuthority> roles = Arrays.stream(roleHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return roles.isEmpty() ? List.of(new SimpleGrantedAuthority("ROLE_USER")) : roles;
    }
}


