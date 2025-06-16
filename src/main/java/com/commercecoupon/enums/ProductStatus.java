package com.commercecoupon.enums;

public enum ProductStatus {
    /**
     * 판매중 - 정상적으로 판매되는 상품
     */
    ACTIVE,

    /**
     * 품절 - 재고가 0인 상품
     */
    OUT_OF_STOCK,

    /**
     * 판매중단 - 관리자가 의도적으로 판매를 중단한 상품
     */
    DISCONTINUED,

    /**
     * 임시품절 - 일시적으로 판매를 중단한 상품
     */
    TEMPORARILY_UNAVAILABLE
}