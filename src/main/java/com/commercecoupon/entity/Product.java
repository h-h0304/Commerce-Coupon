package com.commercecoupon.entity;

import com.commercecoupon.enums.ProductStatus;
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
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String detailImageUrls; // JSON 형태로 여러 이미지 URL 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer salesCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isFeatured = false; // 추천 상품 여부

    @Column(length = 1000)
    private String tags; // 검색용 태그 (콤마 구분)

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 재고 차감 메서드
     */
    public void decreaseStock(Integer quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        this.stock -= quantity;
    }

    /**
     * 재고 증가 메서드 (취소 시 사용)
     */
    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 판매수 증가
     */
    public void increaseSalesCount(Integer quantity) {
        this.salesCount += quantity;
    }
}