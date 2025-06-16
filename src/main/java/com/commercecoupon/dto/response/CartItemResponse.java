package com.commercecoupon.dto.response;

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
@Schema(description = "장바구니 상품 정보 응답")
public class CartItemResponse {

    @Schema(description = "장바구니 아이템 ID", example = "1")
    private Long id;

    @Schema(description = "상품 정보")
    private ProductResponse product;

    @Schema(description = "수량", example = "2")
    private Integer quantity;

    @Schema(description = "상품 단가", example = "25000")
    private Integer unitPrice;

    @Schema(description = "총 금액", example = "50000")
    private Integer totalPrice;

    @Schema(description = "장바구니 추가일시")
    private LocalDateTime createdAt;

    @Schema(description = "수량 변경일시")
    private LocalDateTime updatedAt;
}