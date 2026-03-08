package com.governance.authcenter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 令牌服务。
 * <p>
 * 认证中心通过该服务生成和解析 JWT，
 * 为登录、当前用户查询以及网关鉴权提供统一的令牌能力。
 */
@Service
public class JwtTokenService {

    /**
     * JWT 签名密钥。
     */
    @Value("${auth-center.jwt-secret}")
    private String jwtSecret;

    /**
     * 令牌有效时长，单位为秒。
     */
    @Value("${auth-center.jwt-expire-seconds}")
    private long expireSeconds;

    /**
     * 使用默认角色 USER 生成令牌。
     *
     * @param username 用户名
     * @return JWT 令牌
     */
    public String generateToken(String username) {
        return generateToken(username, "USER");
    }

    /**
     * 生成包含角色信息的 JWT 令牌。
     *
     * @param username 用户名
     * @param role     角色
     * @return JWT 令牌
     */
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(getSigningKey())
                .compact();
    }

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
     * 校验令牌是否有效。
     *
     * @param token JWT 令牌
     * @return true 表示有效
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 从令牌中提取角色。
     *
     * @param token JWT 令牌
     * @return 角色字符串，缺省返回 USER
     */
    public String extractRole(String token) {
        return Optional.ofNullable(extractAllClaims(token).get("role", String.class))
                .filter(role -> !role.isBlank())
                .orElse("USER");
    }

    /**
     * 获取令牌有效期配置。
     *
     * @return 有效时长（秒）
     */
    public long getExpireSeconds() {
        return expireSeconds;
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
     * 解析令牌中的所有声明。
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
     * 构造签名密钥对象。
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
