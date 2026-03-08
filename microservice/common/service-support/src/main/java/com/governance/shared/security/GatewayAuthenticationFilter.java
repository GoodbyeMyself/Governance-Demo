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

@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_GATEWAY_AUTH = "X-Gateway-Auth";
    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_ROLES = "X-Auth-Roles";

    @Value("${security.gateway.trusted-token:change-me-gateway-token}")
    private String trustedGatewayToken;

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

    private boolean shouldSkip(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        return uri.startsWith("/internal/")
                || "/api/auth-center/login".equals(uri)
                || "/api/auth-center/register".equals(uri)
                || "/swagger-ui.html".equals(uri)
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs")
                || "/actuator/health".equals(uri);
    }

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


