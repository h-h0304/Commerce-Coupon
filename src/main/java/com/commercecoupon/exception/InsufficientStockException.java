package com.commercecoupon.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableStock) {
        super(String.format("재고가 부족합니다. 상품: %s, 요청 수량: %d, 재고: %d",
                productName, requestedQuantity, availableStock));
    }
}