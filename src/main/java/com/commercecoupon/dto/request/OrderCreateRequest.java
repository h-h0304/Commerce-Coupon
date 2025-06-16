package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "주문 생성 요청")
public class OrderCreateRequest {

    @Schema(description = "사용할 쿠폰 ID (선택사항)", example = "1")
    private Long couponId;

    @Schema(description = "배송지 정보", required = true)
    @Valid
    private DeliveryInfoRequest deliveryInfo;

    @Schema(description = "주문 메모", example = "빠른 배송 부탁드립니다")
    @Size(max = 500, message = "주문 메모는 500자 이하여야 합니다")
    private String memo;

    @Data
    @Schema(description = "배송지 정보 요청")
    public static class DeliveryInfoRequest {

        @Schema(description = "수취인명", example = "홍길동", required = true)
        @NotBlank(message = "수취인명은 필수입니다")
        @Size(max = 50, message = "수취인명은 50자 이하여야 합니다")
        private String recipientName;

        @Schema(description = "연락처", example = "010-1234-5678", required = true)
        @NotBlank(message = "연락처는 필수입니다")
        @Size(max = 20, message = "연락처는 20자 이하여야 합니다")
        private String phone;

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", required = true)
        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 200, message = "주소는 200자 이하여야 합니다")
        private String address;

        @Schema(description = "상세주소", example = "456호")
        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다")
        private String detailAddress;

        @Schema(description = "우편번호", example = "12345", required = true)
        @NotBlank(message = "우편번호는 필수입니다")
        @Size(max = 10, message = "우편번호는 10자 이하여야 합니다")
        private String zipCode;

        @Schema(description = "배송 요청사항", example = "문앞에 놓아주세요")
        @Size(max = 200, message = "배송 요청사항은 200자 이하여야 합니다")
        private String deliveryMemo;
    }
}