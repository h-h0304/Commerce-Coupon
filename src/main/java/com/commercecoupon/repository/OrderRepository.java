package com.commercecoupon.repository;

import com.commercecoupon.entity.Order;
import com.commercecoupon.entity.User;
import com.commercecoupon.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자별 주문 목록 조회 (페이징)
     */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자 ID로 주문 목록 조회 (페이징)
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 주문번호로 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 사용자와 주문 ID로 조회 (소유권 확인용)
     */
    Optional<Order> findByIdAndUser(Long orderId, User user);

    /**
     * 상태별 주문 조회 (관리자용)
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * 모든 주문 조회 (관리자용)
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 키워드 검색 (주문번호, 사용자명)
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 상태와 키워드로 검색
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND " +
            "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByStatusAndKeyword(@Param("status") OrderStatus status,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    /**
     * 특정 기간 내 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * 상태별 주문 개수 조회
     */
    Long countByStatus(OrderStatus status);

    /**
     * 사용자별 주문 개수 조회
     */
    Long countByUser(User user);

    /**
     * 특정 기간 내 총 매출 조회
     */
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE " +
            "o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED') AND " +
            "o.createdAt BETWEEN :startDate AND :endDate")
    Long sumFinalAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 취소 가능한 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'PAID') " +
            "ORDER BY o.createdAt DESC")
    List<Order> findCancellableOrders();
}