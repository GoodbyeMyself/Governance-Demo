package com.governance.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 服务端统一安全配置。
 *
 * <p>所有业务服务默认共享这套配置，用于保证安全策略一致：
 * 关闭 Session、接入网关透传鉴权、放行健康检查与 Swagger、
 * 并统一返回 JSON 风格的未登录/无权限响应。</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayAuthenticationFilter gatewayAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    /**
     * 构建安全过滤链。
     *
     * <p>这里的核心思路是：
     * <ul>
     *     <li>所有请求默认走无状态鉴权</li>
     *     <li>登录、注册、内部调用、健康检查和 Swagger 文档直接放行</li>
     *     <li>其余请求通过网关透传的头信息建立认证上下文</li>
     * </ul>
     * </p>
     *
     * @param http Spring Security HTTP 配置对象
     * @return 安全过滤链
     * @throws Exception 配置构建异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(
                        exception -> exception
                                .authenticationEntryPoint(restAuthenticationEntryPoint)
                                .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(
                                        "/api/auth-center/captcha",
                                        "/api/auth-center/register",
                                        "/api/auth-center/login",
                                        "/api/auth-center/email-codes/send",
                                        "/api/auth-center/password/reset"
                                ).permitAll()
                                .requestMatchers("/internal/**").permitAll()
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(Customizer.withDefaults());

        return http.build();
    }

    /**
     * 提供统一的密码编码器。
     *
     * <p>当前全系统统一使用 BCrypt，以便注册、登录和管理员初始化逻辑保持一致。</p>
     *
     * @return BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


