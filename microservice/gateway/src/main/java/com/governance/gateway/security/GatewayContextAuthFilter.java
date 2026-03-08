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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 网关鉴权与上下文透传过滤器。
 *
 * <p>该过滤器运行在网关层，负责：
 * <ul>
 *     <li>生成或透传请求 ID</li>
 *     <li>校验前端携带的 JWT</li>
 *     <li>把用户名、角色与内部信任令牌写入下游请求头</li>
 *     <li>对未通过鉴权的请求统一返回 401 JSON</li>
 * </ul>
 * </p>
 */
@Component
public class GatewayContextAuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_ROLES = "X-Auth-Roles";
    private static final String HEADER_GATEWAY_AUTH = "X-Gateway-Auth";
    private static final String HEADER_ACCEPT_LANGUAGE = HttpHeaders.ACCEPT_LANGUAGE;

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

    /**
     * 处理进入网关的请求，并把认证上下文透传到下游服务。
     *
     * @param exchange 当前网关交换对象
     * @param chain 过滤器链
     * @return Reactor 执行结果
     */
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
            return unauthorized(
                    exchange,
                    localizedMessage(exchange, "缺少或无效的 Authorization 请求头", "Missing or invalid Authorization header")
            );
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return unauthorized(
                    exchange,
                    localizedMessage(exchange, "缺少 Bearer 令牌", "Missing bearer token")
            );
        }

        try {
            if (!gatewayJwtService.isTokenValid(token)) {
                return unauthorized(
                        exchange,
                        localizedMessage(exchange, "令牌已过期或无效", "Token expired or invalid")
                );
            }

            String username = gatewayJwtService.extractUsername(token);
            if (!StringUtils.hasText(username)) {
                return unauthorized(
                        exchange,
                        localizedMessage(exchange, "令牌主题无效", "Invalid token subject")
                );
            }

            String role = gatewayJwtService.extractRole(token).toUpperCase(Locale.ROOT);
            builder.header(HEADER_AUTH_USER, username.trim());
            builder.header(HEADER_AUTH_ROLES, "ROLE_" + role);
        } catch (Exception ex) {
            return unauthorized(
                    exchange,
                    localizedMessage(exchange, "令牌解析失败", "Token parse failed")
            );
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    /**
     * 指定过滤器顺序。
     *
     * <p>返回较小的顺序值可以确保鉴权逻辑尽早执行，
     * 避免后续路由与业务过滤器收到未经校验的请求。</p>
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 判断是否属于公共路径。
     *
     * <p>公共路径无需用户登录即可访问，例如登录、注册、Swagger 与健康检查。</p>
     *
     * @param method HTTP 方法
     * @param path 请求路径
     * @return 是否公共路径
     */
    private boolean isPublicPath(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        if (List.of(
                "/api/auth-center/captcha",
                "/api/auth-center/login",
                "/api/auth-center/register",
                "/api/auth-center/email-codes/send",
                "/api/auth-center/password/reset"
        ).contains(path)) {
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

    /**
     * 返回统一的未授权响应。
     *
     * @param exchange 当前网关交换对象
     * @param message 返回给调用方的错误信息
     * @return Reactor 执行结果
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            Map<String, Object> responseBody = new LinkedHashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", message);
            responseBody.put("data", null);
            body = objectMapper.writeValueAsBytes(responseBody);
        } catch (JsonProcessingException ex) {
            body = (
                    "{\"success\":false,\"message\":\""
                            + localizedMessage(exchange, "未授权访问", "Unauthorized")
                            + "\",\"data\":null}"
            )
                    .getBytes(StandardCharsets.UTF_8);
        }

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }

    /**
     * 根据请求头返回本地化文案。
     *
     * @param exchange 当前请求上下文
     * @param zhMessage 中文文案
     * @param enMessage 英文文案
     * @return 当前请求语言对应的文案
     */
    private String localizedMessage(
            ServerWebExchange exchange,
            String zhMessage,
            String enMessage
    ) {
        String acceptLanguage = exchange.getRequest().getHeaders().getFirst(HEADER_ACCEPT_LANGUAGE);
        return StringUtils.hasText(acceptLanguage)
                && acceptLanguage.toLowerCase(Locale.ROOT).startsWith("en")
                ? enMessage
                : zhMessage;
    }
}
