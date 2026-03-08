package com.governance.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class GatewayContextAuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_ROLES = "X-Auth-Roles";
    private static final String HEADER_GATEWAY_AUTH = "X-Gateway-Auth";

    private final GatewayJwtService gatewayJwtService;
    private final ObjectMapper objectMapper;

    public GatewayContextAuthFilter(
            GatewayJwtService gatewayJwtService,
            ObjectMapper objectMapper
    ) {
        this.gatewayJwtService = gatewayJwtService;
        this.objectMapper = objectMapper;
    }

    @Value("${gateway.auth.required-prefix:/api/}")
    private String authRequiredPrefix;

    @Value("${gateway.auth.internal-token:change-me-gateway-token}")
    private String gatewayInternalToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();

        String requestId = request.getHeaders().getFirst(HEADER_REQUEST_ID);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
            builder.header(HEADER_REQUEST_ID, requestId);
        }
        exchange.getResponse().getHeaders().set(HEADER_REQUEST_ID, requestId);
        builder.header(HEADER_GATEWAY_AUTH, gatewayInternalToken);

        String path = request.getURI().getPath();
        if (isPublicPath(request.getMethod(), path) || !path.startsWith(authRequiredPrefix)) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        String authorization = request.getHeaders().getFirst(AUTH_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "Missing bearer token");
        }

        try {
            if (!gatewayJwtService.isTokenValid(token)) {
                return unauthorized(exchange, "Token expired or invalid");
            }

            String username = gatewayJwtService.extractUsername(token);
            if (!StringUtils.hasText(username)) {
                return unauthorized(exchange, "Invalid token subject");
            }

            String role = gatewayJwtService.extractRole(token).toUpperCase(Locale.ROOT);
            builder.header(HEADER_AUTH_USER, username.trim());
            builder.header(HEADER_AUTH_ROLES, "ROLE_" + role);
        } catch (Exception ex) {
            return unauthorized(exchange, "Token parse failed");
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        if ("/api/auth-center/login".equals(path) || "/api/auth-center/register".equals(path)) {
            return true;
        }

        List<String> publicPrefixes = List.of(
                "/auth/",
                "/bms/",
                "/source/",
                "/metadata/",
                "/swagger-ui/",
                "/v3/api-docs/",
                "/actuator/"
        );

        return "/swagger-ui.html".equals(path)
                || "/actuator/health".equals(path)
                || publicPrefixes.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Map.of(
                    "success", false,
                    "message", message,
                    "data", null
            ));
        } catch (JsonProcessingException ex) {
            body = "{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}"
                    .getBytes(StandardCharsets.UTF_8);
        }

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }
}
