package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @Schema(description = "이메일", example = "user@example.com", required = true)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123", required = true)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;

    @Schema(description = "이름", example = "홍길동", required = true)
    @NotBlank(message = "이름은 필수입니다")
    private String name;
}
