package com.commercecouponbe.filter;

import com.commercecoupon.config.JwtConfig;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * H2 Console, Swagger 및 API Docs 경로는 토큰 검증 필터를 통과하지 않도록 처리합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 🔧 모든 제외 경로를 포함
        return path.startsWith("/h2-console")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")           // Swagger 리소스
                || path.startsWith("/error")             // 에러 페이지
                || path.startsWith("/favicon.ico")       // 파비콘
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                // 🔧 중요: API 로그인 경로들
                || path.equals("/api/users/signup")
                || path.equals("/api/users/login")
                || path.equals("/api/auth/signup")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);

            if (token != null && jwtService.validateToken(token)) {
                // 액세스 토큰인지 확인
                String tokenType = jwtService.getTokenType(token);
                if (!"access".equals(tokenType)) {
                    log.warn("리프레시 토큰으로 API 접근 시도: {}", request.getRequestURI());
                    handleJwtException(response, "TOKEN_INVALID", "액세스 토큰이 필요합니다.");
                    return;
                }

                var auth = jwtService.getAuthentication(token);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            handleJwtException(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            handleJwtException(response, "TOKEN_INVALID", "올바르지 않은 토큰입니다.");
            return;
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
            handleJwtException(response, "TOKEN_INVALID", "토큰 검증에 실패했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 JWT 토큰 추출
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConfig.getHeader());
        if (bearerToken != null && bearerToken.startsWith(jwtConfig.getTokenPrefix())) {
            return bearerToken.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * JWT 예외 처리
     */
    private void handleJwtException(HttpServletResponse response,
                                    String errorCode,
                                    String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("WWW-Authenticate", jwtConfig.getTokenPrefix().trim());

        ApiResponse<Object> errorResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}