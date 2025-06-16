package com.commercecoupon.enums;

/**
 * 결제 상태를 정의하는 열거형
 */
public enum PaymentStatus {
    /**
     * 결제 대기 - 결제가 생성되었지만 아직 승인되지 않음
     */
    PENDING,

    /**
     * 결제 완료 - 결제가 성공적으로 승인됨
     */
    COMPLETED,

    /**
     * 결제 실패 - 결제 승인이 실패함
     */
    FAILED,

    /**
     * 결제 취소 - 결제가 취소됨
     */
    CANCELLED,

    /**
     * 부분 환불 - 일부 금액만 환불됨
     */
    PARTIAL_REFUNDED,

    /**
     * 전액 환불 - 전체 금액이 환불됨
     */
    REFUNDED
}