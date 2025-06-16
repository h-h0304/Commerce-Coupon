package com.commercecoupon.dto.response;

import com.commercecoupon.enums.OrderStatus;
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
@Schema(description = "주문 상세 정보 응답")
public class OrderDetailResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "주문번호", example = "ORD-20240101-001")
    private String orderNumber;

    @Schema(description = "주문 상태")
    private OrderStatus status;

    @Schema(description = "주문자 정보")
    private OrderUserInfo user;

    @Schema(description = "주문 상품 목록")
    private List<OrderItemResponse> items;

    @Schema(description = "원래 금액", example = "50000")
    private Integer originalAmount;

    @Schema(description = "쿠폰 할인 금액", example = "5000")
    private Integer couponDiscountAmount;

    @Schema(description = "VIP 할인 금액", example = "2250")
    private Integer vipDiscountAmount;

    @Schema(description = "최종 결제 금액", example = "42750")
    private Integer finalAmount;

    @Schema(description = "사용된 쿠폰 정보")
    private UsedCouponInfo usedCoupon;

    @Schema(description = "배송지 정보")
    private DeliveryInfo deliveryInfo;

    @Schema(description = "주문 메모")
    private String memo;

    @Schema(description = "주문일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderUserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsedCouponInfo {
        @Schema(description = "쿠폰 ID", example = "1")
        private Long id;

        @Schema(description = "쿠폰명", example = "웰컴 쿠폰")
        private String name;

        @Schema(description = "할인 금액", example = "5000")
        private Integer discountAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        @Schema(description = "수취인명", example = "홍길동")
        private String recipientName;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "상세주소", example = "456호")
        private String detailAddress;

        @Schema(description = "우편번호", example = "12345")
        private String zipCode;

        @Schema(description = "배송 요청사항", example = "문앞에 놓아주세요")
        private String deliveryMemo;
    }
}