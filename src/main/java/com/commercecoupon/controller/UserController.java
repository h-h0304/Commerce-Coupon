package com.commercecoupon.controller;

import com.commercecoupon.dto.request.LoginRequest;
import com.commercecoupon.dto.request.SignupRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.LoginResponse;
import com.commercecoupon.dto.response.UserInfoResponse;
import com.commercecoupon.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "유저 API", description = "회원가입, 로그인, 내 정보 조회 등 유저 관련 기능")
public class UserController {

    private final UserService userService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다"));
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    /** 내 권한 조회 */
    @GetMapping("/me/role")
    public ResponseEntity<ApiResponse<String>> getUserRole(Authentication authentication) {
        String role = userService.getUserRole(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("권한 조회 성공", role));
    }

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(Authentication authentication) {
        UserInfoResponse userInfo = userService.getUserInfo(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회 성공", userInfo));
    }

    /** 로그아웃 (프론트에서 토큰 제거만 하면 됨) */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("로그아웃 완료"));
    }
}

