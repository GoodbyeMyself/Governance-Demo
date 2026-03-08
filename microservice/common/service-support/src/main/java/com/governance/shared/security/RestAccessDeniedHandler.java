package com.governance.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.governance.shared.api.ApiResponse;
import com.governance.shared.i18n.MessageResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * REST 场景下的无权限处理器。
 * <p>
 * 当用户已经完成认证，但访问资源所需权限不足时，
 * Spring Security 会回调该处理器输出统一 JSON 响应。
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * JSON 序列化器，用于输出统一响应体。
     */
    private final ObjectMapper objectMapper;
    private final MessageResolver messageResolver;

    /**
     * 处理无权限访问异常。
     *
     * @param request               当前请求
     * @param response              当前响应
     * @param accessDeniedException 无权限异常
     * @throws IOException      输出响应时可能抛出
     * @throws ServletException Servlet 处理异常
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.failure(messageResolver.getMessage("security.accessDenied"))
        );
    }
}
