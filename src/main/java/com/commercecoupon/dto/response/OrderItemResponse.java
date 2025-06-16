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
@Schema(description = "주문 상품 정보 응답")
public class OrderItemResponse {

    @Schema(description = "주문 상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품 정보")
    private ProductResponse product;

    @Schema(description = "주문 수량", example = "2")
    private Integer quantity;

    @Schema(description = "상품 단가", example = "25000")
    private Integer unitPrice;

    @Schema(description = "총 금액", example = "50000")
    private Integer totalPrice;
}