package com.commercecoupon.repository;

import com.commercecoupon.entity.User;
import com.commercecoupon.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 기존 메서드들
     */
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * 🆕 특정 역할의 사용자 목록 조회
     * VIP 회원 목록이나 관리자 목록을 조회할 때 사용
     */
    List<User> findByRole(Role role);

    /**
     * 🆕 특정 역할의 사용자 수 조회
     * VIP 회원 통계를 위해 사용
     */
    Long countByRole(Role role);

    /**
     * 🆕 VIP 승격 대상자 조회
     * 가입일이 특정 기간 이상 된 일반 회원들을 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' " +
            "AND u.createdAt <= :cutoffDate " +
            "ORDER BY u.createdAt ASC")
    List<User> findVipPromotionCandidates(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 🆕 특정 기간 내 가입한 사용자 조회
     * 회원 가입 통계나 이벤트 대상자 선정에 사용
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY u.createdAt DESC")
    List<User> findUsersByJoinDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 🆕 활성 사용자 조회 (최근 업데이트 기준)
     * updatedAt 필드를 사용하여 최근 활동한 사용자를 조회
     */
    @Query("SELECT u FROM User u WHERE u.updatedAt >= :recentDate " +
            "ORDER BY u.updatedAt DESC")
    List<User> findActiveUsers(@Param("recentDate") LocalDateTime recentDate);

    /**
     * 🆕 역할별 최근 가입자 조회
     * 관리자 대시보드에서 최근 VIP 승격자나 신규 가입자를 확인할 때 사용
     */
    @Query("SELECT u FROM User u WHERE u.role = :role " +
            "ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByRole(@Param("role") Role role);

    /**
     * 🆕 이름으로 사용자 검색 (관리자용)
     * 관리자가 특정 회원을 찾을 때 사용
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% " +
            "ORDER BY u.createdAt DESC")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * 🆕 이메일 도메인별 사용자 조회
     * 기업 고객이나 특정 도메인 사용자 분석에 사용
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain " +
            "ORDER BY u.createdAt DESC")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * 🆕 특정 기간 동안 VIP로 승격된 사용자 조회
     * VIP 승격 통계나 이벤트 효과 분석에 사용
     */
    @Query("SELECT u FROM User u WHERE u.role = 'VIP' " +
            "AND u.updatedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY u.updatedAt DESC")
    List<User> findVipPromotionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 🆕 장기 미접속 사용자 조회
     * 휴면 회원 관리나 재활성화 캠페인 대상자 선정에 사용
     */
    @Query("SELECT u FROM User u WHERE u.updatedAt < :cutoffDate " +
            "AND u.role = 'USER' " +
            "ORDER BY u.updatedAt ASC")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 🆕 월별 가입자 통계
     * 관리자 대시보드의 월별 가입자 현황에 사용
     */
    @Query("SELECT YEAR(u.createdAt) as year, MONTH(u.createdAt) as month, " +
            "COUNT(u) as userCount, u.role as role " +
            "FROM User u " +
            "WHERE u.createdAt >= :startDate " +
            "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt), u.role " +
            "ORDER BY YEAR(u.createdAt) DESC, MONTH(u.createdAt) DESC")
    List<Object[]> findMonthlyUserStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 🆕 VIP 회원 중 쿠폰을 많이 사용한 사용자 조회
     * VIP 회원의 쿠폰 사용 패턴 분석에 사용
     */
    @Query("SELECT u FROM User u " +
            "JOIN u.coupons c " +
            "WHERE u.role = 'VIP' " +
            "AND c.isUsed = true " +
            "GROUP BY u.id " +
            "HAVING COUNT(c) >= :minCouponCount " +
            "ORDER BY COUNT(c) DESC")
    List<User> findVipUsersWithHighCouponUsage(@Param("minCouponCount") Long minCouponCount);
}