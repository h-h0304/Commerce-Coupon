package com.commercecoupon.dto.response;

import com.commercecoupon.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 완료 응답")
public class PaymentCompleteResponse {

    @Schema(description = "결제 ID", example = "1")
    private Long paymentId;

    @Schema(description = "결제 키", example = "tgen_20240101123456ABC")
    private String paymentKey;

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "주문번호", example = "ORD-20240101-001")
    private String orderNumber;

    @Schema(description = "결제 금액", example = "42750")
    private Integer amount;

    @Schema(description = "결제 방법", example = "CARD")
    private String paymentMethod;

    @Schema(description = "결제 상태")
    private PaymentStatus status;

    @Schema(description = "결제 승인 시간")
    private LocalDateTime approvedAt;

    @Schema(description = "PG사 거래 ID", example = "202401011234567890")
    private String pgTransactionId;

    @Schema(description = "카드 정보 (마스킹)", example = "신한카드(*1234)")
    private String cardInfo;

    @Schema(description = "결제 생성 시간")
    private LocalDateTime createdAt;
}