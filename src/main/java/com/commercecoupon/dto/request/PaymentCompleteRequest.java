package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "결제 완료 요청")
public class PaymentCompleteRequest {

    @Schema(description = "결제 키", example = "tgen_20240101123456ABC", required = true)
    @NotBlank(message = "결제 키는 필수입니다")
    private String paymentKey;

    @Schema(description = "주문 ID", example = "1", required = true)
    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @Schema(description = "결제 금액", example = "42750", required = true)
    @NotNull(message = "결제 금액은 필수입니다")
    private Integer amount;
}