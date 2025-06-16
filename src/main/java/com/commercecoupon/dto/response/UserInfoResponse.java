package com.commercecoupon.dto.response;

import com.commercecoupon.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserInfoResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "권한", example = "USER")
    private Role role;

    @Schema(description = "가입일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}