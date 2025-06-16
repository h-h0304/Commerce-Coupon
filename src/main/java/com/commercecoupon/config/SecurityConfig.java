package com.commercecoupon.config;

import com.commercecoupon.service.CustomUserDetailsService;
import com.commercecouponbe.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ComponentScan(basePackages = {
        "com.commercecoupon.config",
        "com.commercecouponbe.filter"
})
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // CustomUserDetailsService를 의존성으로 주입받습니다
    // 이 서비스가 실제로 데이터베이스에서 사용자 정보를 조회하는 역할을 담당합니다
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // 기존 API 경로들
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh",

                                // 🔧 Swagger UI 관련 경로들 - 모든 가능한 패턴 포함
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security",
                                "/webjars/**",

                                // 🔧 H2 Console 접근 허용
                                "/h2-console",
                                "/h2-console/**",
                                "/h2-console/login.do",
                                "/h2-console/login.jsp",

                                // 기본 리소스들
                                "/error",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 새로 추가: 인증 제공자를 Spring Security에 등록합니다
                // 이 설정이 있어야 AuthenticationManager가 실제로 인증을 수행할 수 있습니다
                .authenticationProvider(authenticationProvider())

                // 🔧 헤더 설정 개선 - Swagger와 H2 Console을 위한 설정
                .headers(headers -> headers
                        .frameOptions(f -> f.disable()) // H2 Console iframe 허용
                        .contentTypeOptions(c -> c.disable()) // Swagger 리소스 로딩 허용
                        .httpStrictTransportSecurity(h -> h.disable()) // 개발환경에서 HTTPS 강제 비활성화
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 새로 추가: 인증 제공자(DaoAuthenticationProvider) 빈을 생성합니다
     * 이 객체가 실제 인증 로직을 담당하며, UserDetailsService와 PasswordEncoder를 연결하는 역할을 합니다
     *
     * 동작 원리를 단순하게 설명하면:
     * 1. 사용자가 로그인을 시도하면 AuthenticationManager가 이 provider를 호출합니다
     * 2. Provider는 UserDetailsService를 사용해서 사용자 정보를 데이터베이스에서 조회합니다
     * 3. 조회된 사용자 정보와 입력받은 비밀번호를 PasswordEncoder를 사용해서 비교합니다
     * 4. 모든 것이 일치하면 인증 성공, 그렇지 않으면 실패를 반환합니다
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 사용자 정보를 조회할 방법을 설정합니다 (우리가 만든 CustomUserDetailsService)
        authProvider.setUserDetailsService(userDetailsService);
        // 비밀번호를 비교할 방법을 설정합니다 (BCrypt 인코더)
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}