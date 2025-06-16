package com.commercecoupon.service;

import com.commercecoupon.entity.User;
import com.commercecoupon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security에서 사용자 인증 시 호출되는 메서드
     * 이메일(username)을 받아서 데이터베이스에서 사용자를 찾고,
     * Spring Security가 이해할 수 있는 UserDetails 객체로 변환하여 반환
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 데이터베이스에서 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // Spring Security의 User 객체로 변환하여 반환
        // 이 객체가 실제 인증 과정에서 사용됩니다
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())        // 로그인에 사용될 식별자
                .password(user.getPassword())     // 저장된 암호화된 비밀번호
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))  // 사용자 권한
                .accountExpired(false)            // 계정 만료 여부
                .accountLocked(false)             // 계정 잠금 여부
                .credentialsExpired(false)        // 비밀번호 만료 여부
                .disabled(false)                  // 계정 비활성화 여부
                .build();
    }
}
