package com.commercecoupon.repository;

import com.commercecoupon.entity.Payment;
import com.commercecoupon.entity.Order;
import com.commercecoupon.entity.User;
import com.commercecoupon.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 결제 키로 조회
     */
    Optional<Payment> findByPaymentKey(String paymentKey);

    /**
     * 주문으로 결제 조회
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * 주문 ID로 결제 조회
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * 사용자와 결제 ID로 조회 (소유권 확인용)
     */
    Optional<Payment> findByIdAndUser(Long paymentId, User user);

    /**
     * 사용자별 결제 목록 조회
     */
    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 상태별 결제 조회
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * 특정 기간 내 결제 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 상태별 결제 개수 조회
     */
    Long countByStatus(PaymentStatus status);

    /**
     * 특정 기간 내 총 결제 금액 조회
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
            "p.status = 'COMPLETED' AND " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    Long sumAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    /**
     * PG 거래 ID로 조회
     */
    Optional<Payment> findByPgTransactionId(String pgTransactionId);

    /**
     * 결제 키 중복 확인
     */
    boolean existsByPaymentKey(String paymentKey);
}