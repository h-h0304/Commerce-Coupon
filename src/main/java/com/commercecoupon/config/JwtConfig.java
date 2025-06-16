package com.commercecoupon.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    /**
     * JWT 서명에 사용할 비밀키
     * 🚨 환경변수 JWT_SECRET에서 읽어오도록 설정
     */
    private String secret;

    /**
     * 토큰 유효 시간 (초)
     * 기본값: 86400초 (24시간)
     */
    private long tokenValidityInSeconds = 86400;

    /**
     * 리프레시 토큰 유효 시간 (초)
     * 기본값: 604800초 (7일)
     */
    private long refreshTokenValidityInSeconds = 604800;

    /**
     * JWT 토큰 헤더명
     */
    private String header = "Authorization";

    /**
     * JWT 토큰 접두사
     */
    private String tokenPrefix = "Bearer ";

    /**
     * JWT 발행자
     */
    private String issuer = "commerce-coupon-be";

    /**
     * 🆕 초기화 시 비밀키 검증
     */
    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT secret is required. Please set jwt.secret in application.yml or JWT_SECRET environment variable"
            );
        }

        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters long for security. Current length: " + secret.length()
            );
        }
    }

    /**
     * 토큰 유효 시간을 밀리초로 반환
     */
    public long getTokenValidityInMilliseconds() {
        return tokenValidityInSeconds * 1000;
    }

    /**
     * 리프레시 토큰 유효 시간을 밀리초로 반환
     */
    public long getRefreshTokenValidityInMilliseconds() {
        return refreshTokenValidityInSeconds * 1000;
    }
}
