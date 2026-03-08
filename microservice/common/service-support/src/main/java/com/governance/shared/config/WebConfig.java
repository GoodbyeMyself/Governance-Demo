package com.governance.shared.config;

import com.governance.shared.web.ClientRequestHeaderInterceptor;
import com.governance.shared.web.ClientRequestHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 通用配置。
 *
 * <p>当前主要负责两件事：
 * <ul>
 *     <li>为前端访问开放统一的跨域规则</li>
 *     <li>在进入控制器之前把请求头中的链路信息保存到当前请求上下文</li>
 * </ul>
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ClientRequestHeaderInterceptor clientRequestHeaderInterceptor;
    private final String[] allowedOriginPatterns;

    public WebConfig(
            ClientRequestHeaderInterceptor clientRequestHeaderInterceptor,
            @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String[] allowedOriginPatterns
    ) {
        this.clientRequestHeaderInterceptor = clientRequestHeaderInterceptor;
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    /**
     * 为对外 API 注册跨域规则。
     *
     * <p>这里仅对 `/api/**` 生效，避免把内部接口或其他端点暴露给浏览器跨域访问。</p>
     *
     * @param registry Spring MVC 的 CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(ClientRequestHeaders.HEADER_REQUEST_ID);
    }

    /**
     * 注册请求头拦截器。
     *
     * <p>拦截器会把请求 ID、用户标识等头信息提取出来，方便后续日志、异常和下游调用统一使用。</p>
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientRequestHeaderInterceptor)
                .addPathPatterns("/api/**");
    }
}


