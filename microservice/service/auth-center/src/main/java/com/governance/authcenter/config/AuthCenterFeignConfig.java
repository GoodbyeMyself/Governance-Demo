package com.governance.authcenter.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.governance.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.authcenter.exception.AuthCenterDuplicateUserException;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.shared.exception.ResourceNotFoundException;
import com.governance.shared.i18n.MessageResolver;
import com.governance.shared.web.ClientRequestHeaderProvider;
import com.governance.shared.web.ClientRequestHeaders;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * 认证中心 Feign 配置。
 *
 * <p>负责把当前请求语言与链路头透传到下游服务，
 * 同时将下游返回的业务错误转换成认证中心可识别的异常。</p>
 */
@Configuration
@RequiredArgsConstructor
public class AuthCenterFeignConfig {

    private final ClientRequestHeaderProvider clientRequestHeaderProvider;
    private final MessageResolver messageResolver;
    private final ObjectMapper objectMapper;

    /**
     * 透传请求上下文。
     *
     * @return Feign 请求拦截器
     */
    @Bean
    public RequestInterceptor authCenterRequestInterceptor() {
        return template -> {
            copyHeader(template, ClientRequestHeaders.HEADER_REQUEST_ID, clientRequestHeaderProvider.getRequestId());
            copyHeader(template, ClientRequestHeaders.HEADER_REQUEST_TIME, clientRequestHeaderProvider.getRequestTime());
            copyHeader(template, ClientRequestHeaders.HEADER_CLIENT_APP, clientRequestHeaderProvider.getClientApp());
            copyHeader(template, ClientRequestHeaders.HEADER_TENANT_ID, clientRequestHeaderProvider.getTenantId());
            copyHeader(template, ClientRequestHeaders.HEADER_AUTHORIZATION, clientRequestHeaderProvider.getAuthorization());
            copyHeader(template, HttpHeaders.ACCEPT_LANGUAGE, LocaleContextHolder.getLocale().toLanguageTag());
        };
    }

    /**
     * 将下游错误响应映射为统一异常。
     *
     * @return Feign 错误解码器
     */
    @Bean
    public ErrorDecoder authCenterFeignErrorDecoder() {
        return (methodKey, response) -> mapFeignException(response);
    }

    private void copyHeader(RequestTemplate template, String name, String value) {
        if (StringUtils.hasText(value)) {
            template.header(name, value);
        }
    }

    private RuntimeException mapFeignException(Response response) {
        String messageText = extractBusinessMessage(response);
        if (!StringUtils.hasText(messageText)) {
            messageText = messageResolver.getMessage("common.remote.call.failed");
        }

        return switch (response.status()) {
            case 401 -> new AuthCenterAuthenticationException(messageText);
            case 404 -> new ResourceNotFoundException(messageText);
            case 409 -> new AuthCenterDuplicateUserException(messageText);
            default -> new AuthCenterOperationException(messageText);
        };
    }

    private String extractBusinessMessage(Response response) {
        if (response.body() == null) {
            return null;
        }

        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            JsonNode root = objectMapper.readTree(reader);
            JsonNode messageNode = root.path("message");
            if (messageNode.isTextual()) {
                String messageText = messageNode.asText();
                return StringUtils.hasText(messageText) ? messageText.trim() : null;
            }
        } catch (IOException ignored) {
        }

        return null;
    }
}
