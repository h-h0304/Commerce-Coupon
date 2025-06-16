package com.commercecoupon.dto.response;

import com.commercecoupon.enums.OrderStatus;
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
@Schema(description = "주문 요약 정보 응답")
public class OrderSummaryResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "주문번호", example = "ORD-20240101-001")
    private String orderNumber;

    @Schema(description = "주문 상태")
    private OrderStatus status;

    @Schema(description = "주문자명", example = "홍길동")
    private String userName;

    @Schema(description = "주문 상품 수", example = "3")
    private Integer itemCount;

    @Schema(description = "최종 결제 금액", example = "42750")
    private Integer finalAmount;

    @Schema(description = "주문일시")
    private LocalDateTime createdAt;
}
