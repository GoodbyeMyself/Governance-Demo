package com.governance.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * 网关层 JWT 解析服务。
 * <p>
 * 网关在放行请求前通过该服务解析令牌中的用户名与角色信息，
 * 并校验令牌是否过期，从而为后续路由转发补充认证上下文。
 */
@Service
public class GatewayJwtService {

    /**
     * JWT 签名密钥，与认证中心保持一致。
     */
    @Value("${auth-center.jwt-secret}")
    private String jwtSecret;

    /**
     * 从令牌中提取用户名。
     *
     * @param token JWT 令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 从令牌中提取角色。
     *
     * @param token JWT 令牌
     * @return 角色名称，缺省返回 USER
     */
    public String extractRole(String token) {
        return Optional.ofNullable(extractAllClaims(token).get("role", String.class))
                .filter(role -> !role.isBlank())
                .orElse("USER");
    }

    /**
     * 校验令牌是否仍然有效。
     *
     * @param token JWT 令牌
     * @return true 表示有效，false 表示已过期
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 判断令牌是否过期。
     *
     * @param token JWT 令牌
     * @return true 表示已过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 解析令牌中的全部声明信息。
     *
     * @param token JWT 令牌
     * @return Claims 对象
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 构造 JWT 签名密钥。
     *
     * @return 签名密钥对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
