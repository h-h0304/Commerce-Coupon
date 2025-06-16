package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "결제 준비 요청")
public class PaymentPrepareRequest {

    @Schema(description = "주문 ID", example = "1", required = true)
    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @Schema(description = "결제 방법", example = "CARD", required = true)
    @NotBlank(message = "결제 방법은 필수입니다")
    private String paymentMethod;

    @Schema(description = "결제 금액", example = "42750", required = true)
    @NotNull(message = "결제 금액은 필수입니다")
    private Integer amount;
}