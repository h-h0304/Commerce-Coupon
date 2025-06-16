package com.commercecoupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer totalPrice;

    // 주문 당시의 상품 정보 스냅샷 (상품 정보 변경 시에도 주문 내역 유지)
    @Column(nullable = false, length = 200)
    private String productName;

    @Column(length = 500)
    private String productImageUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 장바구니 아이템으로부터 주문 아이템 생성
     */
    public static OrderItem fromCartItem(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return OrderItem.builder()
                .product(product)
                .quantity(cartItem.getQuantity())
                .unitPrice(product.getPrice())
                .totalPrice(cartItem.getTotalPrice())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .build();
    }
}