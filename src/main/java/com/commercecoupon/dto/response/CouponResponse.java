package com.commercecoupon.dto.response;

import com.commercecoupon.enums.CouponType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "쿠폰 정보 응답")
public class CouponResponse {

    @Schema(description = "쿠폰 ID", example = "1")
    private Long id;

    @Schema(description = "쿠폰명", example = "웰컴 쿠폰")
    private String name;

    @Schema(description = "쿠폰 타입", example = "WELCOME")
    private CouponType type;

    @Schema(description = "할인 금액", example = "5000")
    private Integer discountAmount;

    @Schema(description = "할인 퍼센트", example = "10")
    private Integer discountPercent;

    @Schema(description = "만료일시", example = "2024-02-01T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "사용 여부", example = "false")
    private Boolean isUsed;

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}
