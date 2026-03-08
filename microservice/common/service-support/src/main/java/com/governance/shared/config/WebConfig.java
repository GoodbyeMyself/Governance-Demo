package com.governance.shared.config;

import com.governance.shared.web.ClientRequestHeaderInterceptor;
import com.governance.shared.web.ClientRequestHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(ClientRequestHeaders.HEADER_REQUEST_ID);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientRequestHeaderInterceptor)
                .addPathPatterns("/api/**");
    }
}


