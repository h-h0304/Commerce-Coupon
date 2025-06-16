package com.commercecoupon.service;

import com.commercecoupon.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long tokenValidityInSeconds;
    private final long refreshTokenValidityInSeconds;

    public JwtService(JwtConfig jwtConfig) {
        // application.yml의 secret 키를 바이트로 변환해 SecretKey 생성
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        this.tokenValidityInSeconds = jwtConfig.getTokenValidityInSeconds();
        this.refreshTokenValidityInSeconds = jwtConfig.getRefreshTokenValidityInSeconds();
    }

    /** 토큰 유효성 검사 (서명 검증 + 만료 검사) */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 토큰 타입(access or refresh) 조회 */
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tokenType", String.class);
    }

    /** Spring Security Authentication 객체로 변환 */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        String role  = claims.get("role", String.class);

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /** 액세스 토큰 생성 (tokenValidityInSeconds초 후 만료) */
    public String generateAccessToken(String email, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("tokenType", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 리프레시 토큰 생성 (refreshTokenValidityInSeconds초 후 만료) */
    public String generateRefreshToken(String email) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(email)
                .claim("tokenType", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 액세스 토큰 유효시간(초) 반환 */
    public long getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }

    /** 토큰 파싱 후 Claims 반환 (만료된 토큰일 경우 예외 발생) */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
