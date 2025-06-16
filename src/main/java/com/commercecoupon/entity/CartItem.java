package com.commercecoupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 해당 아이템의 총 금액 계산
     */
    public Integer getTotalPrice() {
        return product.getPrice() * quantity;
    }

    /**
     * 수량 증가
     */
    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
    }

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
        this.quantity = newQuantity;
    }

    /**
     * 재고 확인
     */
    public boolean isStockAvailable() {
        return product.getStock() >= quantity;
    }

    /**
     * 재고 부족 수량 계산
     */
    public Integer getShortageQuantity() {
        return Math.max(0, quantity - product.getStock());
    }
}