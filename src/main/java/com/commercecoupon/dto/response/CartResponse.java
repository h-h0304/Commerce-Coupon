package com.commercecoupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 정보 응답")
public class CartResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "장바구니 상품 목록")
    private List<CartItemResponse> items;

    @Schema(description = "총 상품 수", example = "3")
    private Integer totalItemCount;

    @Schema(description = "총 상품 금액", example = "75000")
    private Integer totalAmount;

    @Schema(description = "예상 VIP 할인 금액", example = "3750")
    private Integer expectedVipDiscount;

    @Schema(description = "최종 예상 금액 (VIP 할인 적용)", example = "71250")
    private Integer expectedFinalAmount;

    @Schema(description = "마지막 수정일시")
    private LocalDateTime updatedAt;
}