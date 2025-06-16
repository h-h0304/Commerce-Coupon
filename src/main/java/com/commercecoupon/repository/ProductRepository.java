package com.commercecoupon.repository;

import com.commercecoupon.entity.Product;
import com.commercecoupon.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ==================== 일반 사용자용 쿼리 메서드들 ====================

    /**
     * 상태별 상품 조회 (페이징)
     */
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

    /**
     * 카테고리별 활성 상품 조회
     */
    Page<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(
            Long categoryId, ProductStatus status, Pageable pageable);

    /**
     * 상품명으로 검색 (대소문자 무시, 부분 일치)
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Product> findByKeywordAndStatus(@Param("keyword") String keyword,
                                         @Param("status") ProductStatus status,
                                         Pageable pageable);

    /**
     * 가격 범위로 상품 검색
     */
    Page<Product> findByStatusAndPriceBetweenOrderByCreatedAtDesc(
            ProductStatus status, Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 추천 상품 조회
     */
    List<Product> findByStatusAndIsFeaturedTrueOrderBySalesCountDesc(ProductStatus status);

    /**
     * 인기 상품 조회 (판매량 기준)
     */
    List<Product> findTop10ByStatusOrderBySalesCountDesc(ProductStatus status);

    /**
     * 최신 상품 조회
     */
    List<Product> findTop10ByStatusOrderByCreatedAtDesc(ProductStatus status);

    /**
     * 관련 상품 조회 (같은 카테고리)
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
            "AND p.status = :status AND p.id != :excludeId " +
            "ORDER BY p.salesCount DESC")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId,
                                      @Param("status") ProductStatus status,
                                      @Param("excludeId") Long excludeId,
                                      Pageable pageable);

    // ==================== 관리자용 쿼리 메서드들 ====================

    /**
     * 모든 상품 조회 (관리자용) - 상태 관계없이 모든 상품
     */
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 카테고리별 모든 상품 조회 (관리자용) - 상태 관계없이
     */
    Page<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    /**
     * 가격 범위로 모든 상품 검색 (관리자용) - 상태 관계없이
     */
    Page<Product> findByPriceBetweenOrderByCreatedAtDesc(Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 키워드로 모든 상품 검색 (관리자용) - 상태 관계없이
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Product> findByKeywordIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 재고 부족 상품 조회 (관리자용)
     */
    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold " +
            "AND p.status = 'ACTIVE' ORDER BY p.stock ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // ==================== 공통 유틸리티 메서드들 ====================

    /**
     * 카테고리별 상품 수 조회
     */
    Long countByCategoryIdAndStatus(Long categoryId, ProductStatus status);

    /**
     * 상품명 중복 확인
     */
    boolean existsByName(String name);
}