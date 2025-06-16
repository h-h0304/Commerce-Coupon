package com.commercecoupon.service;

import com.commercecoupon.dto.request.LoginRequest;
import com.commercecoupon.dto.request.SignupRequest;
import com.commercecoupon.dto.response.LoginResponse;
import com.commercecoupon.dto.response.UserInfoResponse;
import com.commercecoupon.entity.User;
import com.commercecoupon.enums.Role;
import com.commercecoupon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j  // 로깅을 위한 어노테이션 추가 - 문제 발생 시 추적과 디버깅에 필수적입니다
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용 트랜잭션으로 설정하여 성능을 최적화합니다
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    // 🆕 웰컴 쿠폰 자동 발급을 위한 CouponService 의존성 추가
    private final CouponService couponService;

    /**
     * 회원가입 처리 메서드
     *
     * @Transactional을 명시적으로 추가하는 이유:
     * 데이터베이스에 새로운 사용자를 저장하는 작업은 쓰기 작업이므로
     * 읽기 전용이 아닌 일반 트랜잭션이 필요합니다.
     * 만약 회원가입 과정에서 오류가 발생한다면, 트랜잭션이 롤백되어
     * 불완전한 데이터가 데이터베이스에 남지 않도록 보장합니다.
     */
    @Transactional
    public void signup(SignupRequest req) {
        // 비즈니스 로직 시작을 로그로 기록합니다
        // 실제 운영 환경에서는 이런 로그가 시스템 모니터링과 문제 해결에 큰 도움이 됩니다
        log.info("회원가입 시도: email={}", req.getEmail());

        // 이메일 중복 검사 - 데이터 무결성을 보장하는 핵심 로직입니다
        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("회원가입 실패 - 중복된 이메일: {}", req.getEmail());
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        try {
            // User 엔티티 생성 시 시간 정보도 함께 설정합니다
            // 이 정보들은 나중에 사용자 관리나 감사(audit) 목적으로 활용됩니다
            User user = User.builder()
                    .email(req.getEmail())
                    .password(passwordEncoder.encode(req.getPassword()))  // 보안을 위해 반드시 암호화해서 저장
                    .name(req.getName())
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())  // 생성 시간 명시적 설정
                    .updatedAt(LocalDateTime.now())  // 수정 시간 명시적 설정
                    .build();

            // 사용자 정보 저장
            User savedUser = userRepository.save(user);
            log.info("사용자 저장 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

            // 🆕 웰컴 쿠폰 자동 발급
            // 회원가입이 성공적으로 완료된 후 웰컴 쿠폰을 자동으로 발급합니다
            // 만약 쿠폰 발급 중 오류가 발생하면 전체 트랜잭션이 롤백됩니다
            try {
                couponService.issueWelcomeCoupon(savedUser);
                log.info("웰컴 쿠폰 발급 완료: userId={}", savedUser.getId());
            } catch (Exception couponError) {
                // 쿠폰 발급 실패 시 로그를 남기고 예외를 재발생시켜 전체 트랜잭션을 롤백합니다
                log.error("웰컴 쿠폰 발급 실패: userId={}, error={}", savedUser.getId(), couponError.getMessage(), couponError);
                throw new RuntimeException("웰컴 쿠폰 발급 중 오류가 발생했습니다", couponError);
            }

            log.info("회원가입 성공: email={}, name={}", req.getEmail(), req.getName());

        } catch (Exception e) {
            // 예상치 못한 오류 발생 시 로그를 남기고 예외를 재발생시킵니다
            log.error("회원가입 중 오류 발생: email={}, error={}", req.getEmail(), e.getMessage(), e);

            // 이미 RuntimeException인 경우 그대로 재발생
            if (e instanceof RuntimeException) {
                throw e;
            }
            // 그렇지 않은 경우 RuntimeException으로 래핑
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 로그인 처리 메서드
     *
     * 읽기 전용 작업이지만 인증 과정이 포함되므로 별도의 트랜잭션 설정은 하지 않습니다.
     * AuthenticationManager가 내부적으로 적절한 트랜잭션 관리를 수행합니다.
     */
    public LoginResponse login(LoginRequest req) {
        log.info("로그인 시도: email={}", req.getEmail());

        try {
            // 1) Spring Security를 통한 인증 수행
            // 이 과정에서 CustomUserDetailsService가 호출되어 사용자 정보를 조회하고
            // 비밀번호 검증이 이루어집니다
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            log.debug("인증 성공: email={}", req.getEmail());

            // 2) 인증이 성공했다면 사용자 정보를 다시 조회합니다
            // 이는 JWT 토큰에 포함할 최신 사용자 정보를 얻기 위함입니다
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> {
                        log.error("인증 후 사용자 조회 실패: email={}", req.getEmail());
                        return new IllegalArgumentException("회원이 없습니다");
                    });

            // 3) JWT 토큰 생성
            // 액세스 토큰과 리프레시 토큰을 모두 생성하여 클라이언트에게 제공합니다
            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            log.info("로그인 성공: email={}, role={}", user.getEmail(), user.getRole());

            // 4) 클라이언트가 필요한 모든 정보를 포함한 응답 DTO 생성
            return LoginResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getTokenValidityInSeconds())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException e) {
            // 인증 실패 시 구체적인 로그를 남기되, 보안을 위해 클라이언트에게는 일반적인 메시지만 전달
            log.warn("로그인 실패 - 인증 오류: email={}, reason={}", req.getEmail(), e.getMessage());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        } catch (Exception e) {
            // 기타 예상치 못한 오류 처리
            log.error("로그인 처리 중 오류 발생: email={}, error={}", req.getEmail(), e.getMessage(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 사용자 권한 조회 메서드
     * JWT 토큰에서 추출한 이메일을 사용하여 현재 사용자의 권한을 조회합니다
     */
    public String getUserRole(String email) {
        log.debug("사용자 권한 조회: email={}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("권한 조회 실패 - 사용자 없음: email={}", email);
                    return new IllegalArgumentException("회원이 없습니다");
                })
                .getRole()
                .name();
    }

    /**
     * 사용자 정보 조회 메서드
     * JWT 토큰에서 추출한 이메일을 사용하여 현재 사용자의 상세 정보를 조회합니다
     *
     * 보안상 중요한 정보(비밀번호 등)는 응답에 포함하지 않습니다
     */
    public UserInfoResponse getUserInfo(String email) {
        log.debug("사용자 정보 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("사용자 정보 조회 실패 - 사용자 없음: email={}", email);
                    return new IllegalArgumentException("회원이 없습니다");
                });

        // 클라이언트에게 안전한 정보만 전달하는 DTO 생성
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}