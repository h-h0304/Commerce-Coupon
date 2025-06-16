package com.commercecoupon.repository;

import com.commercecoupon.entity.Coupon;
import com.commercecoupon.enums.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * 사용자의 모든 쿠폰 조회 (최신순)
     */
    List<Coupon> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 특정 타입 쿠폰 존재 여부 확인
     * 웰컴 쿠폰 중복 발급 방지를 위해 사용
     */
    boolean existsByUserIdAndType(Long userId, CouponType type);

    /**
     * 사용 가능한 쿠폰만 조회 (사용되지 않고, 만료되지 않은 쿠폰)
     * 결제 시 사용할 수 있는 쿠폰 목록을 가져올 때 사용
     */
    @Query("SELECT c FROM Coupon c WHERE c.user.id = :userId " +
            "AND c.isUsed = false " +
            "AND c.expiryDate > :currentDateTime " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findAvailableCouponsByUserId(@Param("userId") Long userId,
                                              @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * 특정 타입의 사용 가능한 쿠폰 조회
     * 특정 타입의 쿠폰만 필터링할 때 사용 (예: WELCOME, SPECIAL 등)
     */
    @Query("SELECT c FROM Coupon c WHERE c.user.id = :userId " +
            "AND c.type = :type " +
            "AND c.isUsed = false " +
            "AND c.expiryDate > :currentDateTime " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findAvailableCouponsByUserIdAndType(@Param("userId") Long userId,
                                                     @Param("type") CouponType type,
                                                     @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * 만료된 쿠폰 조회
     * 배치 작업으로 만료된 쿠폰을 정리할 때 사용
     */
    @Query("SELECT c FROM Coupon c WHERE c.expiryDate < :currentDateTime " +
            "AND c.isUsed = false")
    List<Coupon> findExpiredCoupons(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * 사용자의 사용된 쿠폰 조회
     * 쿠폰 사용 내역을 확인할 때 사용
     * updatedAt → createdAt으로 수정 (Coupon 엔티티에 updatedAt 필드 없음)
     */
    @Query("SELECT c FROM Coupon c WHERE c.user.id = :userId " +
            "AND c.isUsed = true " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findUsedCouponsByUserId(@Param("userId") Long userId);

    /**
     * 특정 기간 내 발급된 쿠폰 조회
     * 관리자가 쿠폰 발급 통계를 확인할 때 사용
     */
    @Query("SELECT c FROM Coupon c WHERE c.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findCouponsByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 타입 쿠폰의 총 발급 수량 조회
     * 쿠폰 발급 제한을 확인할 때 사용 (향후 선착순 기능에서 활용)
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.type = :type")
    Long countByType(@Param("type") CouponType type);

    /**
     * VIP 회원의 쿠폰 조회
     * VIP 전용 쿠폰 관리 시 사용
     */
    @Query("SELECT c FROM Coupon c WHERE c.user.role = 'VIP' " +
            "AND c.type = :type " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findVipCouponsByType(@Param("type") CouponType type);

    /**
     * 사용자별 쿠폰 개수 조회
     * 사용자가 가진 쿠폰 수를 확인할 때 사용
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 사용자별 사용 가능한 쿠폰 개수 조회
     * 사용자가 현재 사용할 수 있는 쿠폰 수를 확인할 때 사용
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.user.id = :userId " +
            "AND c.isUsed = false " +
            "AND c.expiryDate > :currentDateTime")
    Long countAvailableCouponsByUserId(@Param("userId") Long userId,
                                       @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * 특정 기간 내 사용된 쿠폰 조회
     * 쿠폰 사용 통계 분석에 사용
     */
    @Query("SELECT c FROM Coupon c WHERE c.isUsed = true " +
            "AND c.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY c.createdAt DESC")
    List<Coupon> findUsedCouponsByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}