package com.commercecoupon.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long productId) {
        super("상품을 찾을 수 없습니다. ID: " + productId);
    }
}