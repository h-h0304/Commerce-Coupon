package com.commercecoupon.repository;

import com.commercecoupon.entity.Order;
import com.commercecoupon.entity.OrderItem;
import com.commercecoupon.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 주문별 아이템 조회
     */
    List<OrderItem> findByOrderOrderByCreatedAtAsc(Order order);

    /**
     * 주문 ID로 아이템 조회
     */
    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    /**
     * 상품별 판매 통계
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED') " +
            "GROUP BY oi.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findProductSalesStatistics();

    /**
     * 특정 기간 인기 상품 조회
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED') " +
            "AND oi.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findPopularProductsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 상품의 총 판매량 조회
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi " +
            "WHERE oi.product = :product " +
            "AND oi.order.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED')")
    Long sumQuantityByProduct(@Param("product") Product product);
}