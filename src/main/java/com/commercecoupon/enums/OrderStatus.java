package com.commercecoupon.enums;

/**
 * 주문 상태를 정의하는 열거형
 */
public enum OrderStatus {
    /**
     * 주문 접수 - 주문이 생성된 초기 상태
     */
    PENDING,

    /**
     * 결제 완료 - 결제가 성공적으로 완료됨
     */
    PAID,

    /**
     * 배송 준비중 - 상품을 포장하고 배송을 준비하는 단계
     */
    PREPARING,

    /**
     * 배송중 - 상품이 배송 중인 상태
     */
    SHIPPED,

    /**
     * 배송 완료 - 상품이 고객에게 성공적으로 배송됨
     */
    DELIVERED,

    /**
     * 주문 취소 - 고객이나 관리자가 주문을 취소함
     */
    CANCELLED,

    /**
     * 환불 처리중 - 환불 요청이 들어와서 처리 중인 상태
     */
    REFUND_REQUESTED,

    /**
     * 환불 완료 - 환불이 완료됨
     */
    REFUNDED
}