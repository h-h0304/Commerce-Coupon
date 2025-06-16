package com.commercecoupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 준비 응답")
public class PaymentPrepareResponse {

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

    @Schema(description = "결제 요청 URL", example = "https://payment.toss.live/v1/payment")
    private String paymentUrl;

    @Schema(description = "성공 콜백 URL", example = "http://localhost:3000/success")
    private String successUrl;

    @Schema(description = "실패 콜백 URL", example = "http://localhost:3000/fail")
    private String failUrl;
}