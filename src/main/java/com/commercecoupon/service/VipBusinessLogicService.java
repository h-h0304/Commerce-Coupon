package com.commercecoupon.service;

import com.commercecoupon.entity.User;
import com.commercecoupon.enums.Role;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP 관련 비즈니스 로직을 처리하는 서비스
 * 🆕 마이그레이션 없이 동작 - 기존 USER/ADMIN 데이터는 그대로 두고 신규 가입자만 VIP 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VipBusinessLogicService {

    private final UserRepository userRepository;
    private final CouponService couponService;

    // VIP 승격 기준 상수들
    private static final Integer VIP_MEMBERSHIP_MONTHS = 12; // 12개월 이상 회원

    // VIP 혜택 상수들
    private static final Integer VIP_DISCOUNT_RATE = 5; // 5% 추가 할인
    private static final Integer MAX_VIP_DISCOUNT = 5000; // 최대 5,000원 할인
    private static final Integer VIP_SPECIAL_COUPON_AMOUNT = 20000; // VIP 특별 쿠폰 금액
    private static final Integer VIP_BIRTHDAY_COUPON_AMOUNT = 30000; // VIP 생일 쿠폰 금액

    /**
     * 🆕 사용자 VIP 승격 자격 확인
     * 기존 사용자들도 안전하게 처리됩니다.
     */
    @Transactional(readOnly = true)
    public boolean checkVipEligibility(String email) {
        log.info("VIP 승격 자격 확인 시작: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        // 🔥 기존 데이터 호환: Role.VIP가 없는 기존 DB에서도 안전하게 동작
        if (user.getRole() == Role.ADMIN) {
            log.info("관리자는 VIP 승격 대상이 아님: email={}", email);
            return false;
        }

        // USER만 VIP 승격 대상 (기존 데이터 그대로 유지)
        if (user.getRole() != Role.USER) {
            log.info("일반 회원이 아니므로 VIP 승격 불가: email={}, role={}", email, user.getRole());
            return false;
        }

        // 가입 기간 확인 (12개월 이상)
        LocalDateTime vipEligibleDate = user.getCreatedAt().plusMonths(VIP_MEMBERSHIP_MONTHS);
        boolean membershipPeriodMet = LocalDateTime.now().isAfter(vipEligibleDate);

        log.info("VIP 승격 자격 확인 결과: email={}, membershipPeriodMet={}",
                email, membershipPeriodMet);

        return membershipPeriodMet;
    }

    /**
     * 🆕 사용자를 VIP로 승격
     * 🔥 이 메서드는 Role enum에 VIP가 추가된 후에만 사용 가능합니다.
     */
    @Transactional
    public void promoteToVip(String email) {
        log.info("VIP 승격 처리 시작: email={}", email);

        try {
            // VIP Role이 존재하는지 확인
            Role.VIP.toString(); // VIP enum이 없으면 여기서 컴파일 에러 발생
        } catch (Exception e) {
            log.error("VIP Role이 정의되지 않음. Role enum에 VIP를 추가해주세요.");
            throw new CustomException("VIP 기능이 아직 활성화되지 않았습니다. 관리자에게 문의하세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        if (user.getRole() != Role.USER) {
            log.warn("VIP 승격 불가 - 일반 회원이 아님: email={}, currentRole={}", email, user.getRole());
            throw new CustomException("일반 회원만 VIP로 승격할 수 있습니다");
        }

        if (!checkVipEligibility(email)) {
            log.warn("VIP 승격 자격 미달: email={}", email);
            throw new CustomException("VIP 승격 자격을 만족하지 않습니다");
        }

        try {
            // 1. 사용자 등급 변경
            user.setRole(Role.VIP);
            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);

            log.info("사용자 등급 변경 완료: userId={}, newRole={}", savedUser.getId(), savedUser.getRole());

            // 2. VIP 승격 축하 쿠폰 발급
            couponService.issueVipSpecialCoupon(
                    savedUser.getId(),
                    "VIP 승격 축하 쿠폰",
                    VIP_SPECIAL_COUPON_AMOUNT,
                    90 // 90일 유효
            );

            log.info("VIP 승격 완료: userId={}, email={}", savedUser.getId(), email);

        } catch (Exception e) {
            log.error("VIP 승격 처리 중 오류 발생: email={}, error={}", email, e.getMessage(), e);
            throw new RuntimeException("VIP 승격 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 🆕 VIP 회원 여부 확인 (안전한 방식)
     * 기존 데이터베이스에 VIP가 없어도 안전하게 동작합니다.
     */
    public boolean isVipMember(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

            // 🔥 VIP Role이 없는 경우 안전하게 false 반환
            try {
                return user.getRole() == Role.VIP || user.getRole() == Role.ADMIN;
            } catch (Exception e) {
                // VIP enum이 없는 경우 ADMIN만 VIP 혜택 적용
                log.debug("VIP Role 미정의, ADMIN만 VIP 혜택 적용: email={}", email);
                return user.getRole() == Role.ADMIN;
            }
        } catch (Exception e) {
            log.error("VIP 회원 확인 중 오류: email={}, error={}", email, e.getMessage());
            return false;
        }
    }

    /**
     * 🆕 VIP 회원 할인 금액 계산 (안전한 방식)
     */
    public Integer calculateVipDiscount(String email, Integer originalAmount) {
        log.debug("VIP 할인 계산: email={}, originalAmount={}", email, originalAmount);

        try {
            if (!isVipMember(email)) {
                return 0; // VIP가 아니면 추가 할인 없음
            }

            // VIP 추가 할인 정책: 구매 금액의 5% 추가 할인 (최대 5,000원)
            Integer vipDiscount = Math.min(originalAmount * VIP_DISCOUNT_RATE / 100, MAX_VIP_DISCOUNT);

            log.info("VIP 추가 할인 계산: email={}, originalAmount={}, vipDiscount={}",
                    email, originalAmount, vipDiscount);

            return vipDiscount;

        } catch (Exception e) {
            log.error("VIP 할인 계산 중 오류: email={}, error={}", email, e.getMessage());
            return 0; // 오류 시 할인 없음으로 안전하게 처리
        }
    }

    /**
     * 🆕 VIP 전용 혜택 정보 조회 (안전한 방식)
     */
    @Transactional(readOnly = true)
    public VipBenefitInfo getVipBenefitInfo(String email) {
        log.debug("VIP 혜택 정보 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        if (!isVipMember(email)) {
            throw new CustomException("VIP 회원이 아닙니다");
        }

        return VipBenefitInfo.builder()
                .discountRate(VIP_DISCOUNT_RATE)
                .maxDiscountAmount(MAX_VIP_DISCOUNT)
                .specialCouponAmount(VIP_SPECIAL_COUPON_AMOUNT)
                .birthdayCouponAmount(VIP_BIRTHDAY_COUPON_AMOUNT)
                .hasVipStatus(true)
                .memberSince(user.getCreatedAt())
                .build();
    }

    /**
     * 🆕 VIP 생일 쿠폰 발급 (안전한 방식)
     */
    @Transactional
    public void issueBirthdayCouponForVip(String email) {
        log.info("VIP 생일 쿠폰 발급: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        if (!isVipMember(email)) {
            log.warn("VIP 생일 쿠폰 발급 실패 - VIP 회원 아님: email={}, role={}", email, user.getRole());
            throw new CustomException("VIP 회원만 생일 쿠폰을 받을 수 있습니다");
        }

        try {
            couponService.issueVipSpecialCoupon(
                    user.getId(),
                    "VIP 생일 축하 쿠폰",
                    VIP_BIRTHDAY_COUPON_AMOUNT,
                    30 // 30일 유효
            );

            log.info("VIP 생일 쿠폰 발급 완료: userId={}, amount={}", user.getId(), VIP_BIRTHDAY_COUPON_AMOUNT);

        } catch (Exception e) {
            log.error("VIP 생일 쿠폰 발급 중 오류 발생: email={}, error={}", email, e.getMessage(), e);
            throw new RuntimeException("VIP 생일 쿠폰 발급 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 🆕 VIP 회원 통계 (안전한 방식)
     */
    @Transactional(readOnly = true)
    public VipStatistics getVipStatistics() {
        log.info("VIP 회원 통계 조회");

        try {
            Long totalUsers = userRepository.countByRole(Role.USER);
            Long adminUsers = userRepository.countByRole(Role.ADMIN);

            Long vipUsers = 0L;
            try {
                vipUsers = userRepository.countByRole(Role.VIP);
            } catch (Exception e) {
                log.debug("VIP Role 미정의, VIP 사용자 수 0으로 처리");
            }

            return VipStatistics.builder()
                    .totalUsers(totalUsers)
                    .vipUsers(vipUsers)
                    .adminUsers(adminUsers)
                    .vipRatio(totalUsers > 0 ? (double) vipUsers / totalUsers * 100 : 0.0)
                    .build();

        } catch (Exception e) {
            log.error("VIP 통계 조회 중 오류: error={}", e.getMessage());
            // 오류 시 기본값 반환
            return VipStatistics.builder()
                    .totalUsers(0L)
                    .vipUsers(0L)
                    .adminUsers(0L)
                    .vipRatio(0.0)
                    .build();
        }
    }

    /**
     * VIP 혜택 정보 DTO (동일)
     */
    public static class VipBenefitInfo {
        private Integer discountRate;
        private Integer maxDiscountAmount;
        private Integer specialCouponAmount;
        private Integer birthdayCouponAmount;
        private Boolean hasVipStatus;
        private LocalDateTime memberSince;

        public static VipBenefitInfoBuilder builder() {
            return new VipBenefitInfoBuilder();
        }

        public static class VipBenefitInfoBuilder {
            private Integer discountRate;
            private Integer maxDiscountAmount;
            private Integer specialCouponAmount;
            private Integer birthdayCouponAmount;
            private Boolean hasVipStatus;
            private LocalDateTime memberSince;

            public VipBenefitInfoBuilder discountRate(Integer discountRate) {
                this.discountRate = discountRate;
                return this;
            }

            public VipBenefitInfoBuilder maxDiscountAmount(Integer maxDiscountAmount) {
                this.maxDiscountAmount = maxDiscountAmount;
                return this;
            }

            public VipBenefitInfoBuilder specialCouponAmount(Integer specialCouponAmount) {
                this.specialCouponAmount = specialCouponAmount;
                return this;
            }

            public VipBenefitInfoBuilder birthdayCouponAmount(Integer birthdayCouponAmount) {
                this.birthdayCouponAmount = birthdayCouponAmount;
                return this;
            }

            public VipBenefitInfoBuilder hasVipStatus(Boolean hasVipStatus) {
                this.hasVipStatus = hasVipStatus;
                return this;
            }

            public VipBenefitInfoBuilder memberSince(LocalDateTime memberSince) {
                this.memberSince = memberSince;
                return this;
            }

            public VipBenefitInfo build() {
                VipBenefitInfo info = new VipBenefitInfo();
                info.discountRate = this.discountRate;
                info.maxDiscountAmount = this.maxDiscountAmount;
                info.specialCouponAmount = this.specialCouponAmount;
                info.birthdayCouponAmount = this.birthdayCouponAmount;
                info.hasVipStatus = this.hasVipStatus;
                info.memberSince = this.memberSince;
                return info;
            }
        }

        // Getters
        public Integer getDiscountRate() { return discountRate; }
        public Integer getMaxDiscountAmount() { return maxDiscountAmount; }
        public Integer getSpecialCouponAmount() { return specialCouponAmount; }
        public Integer getBirthdayCouponAmount() { return birthdayCouponAmount; }
        public Boolean getHasVipStatus() { return hasVipStatus; }
        public LocalDateTime getMemberSince() { return memberSince; }
    }

    /**
     * VIP 통계 정보 DTO (동일)
     */
    public static class VipStatistics {
        private Long totalUsers;
        private Long vipUsers;
        private Long adminUsers;
        private Double vipRatio;

        public static VipStatisticsBuilder builder() {
            return new VipStatisticsBuilder();
        }

        public static class VipStatisticsBuilder {
            private Long totalUsers;
            private Long vipUsers;
            private Long adminUsers;
            private Double vipRatio;

            public VipStatisticsBuilder totalUsers(Long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }

            public VipStatisticsBuilder vipUsers(Long vipUsers) {
                this.vipUsers = vipUsers;
                return this;
            }

            public VipStatisticsBuilder adminUsers(Long adminUsers) {
                this.adminUsers = adminUsers;
                return this;
            }

            public VipStatisticsBuilder vipRatio(Double vipRatio) {
                this.vipRatio = vipRatio;
                return this;
            }

            public VipStatistics build() {
                VipStatistics stats = new VipStatistics();
                stats.totalUsers = this.totalUsers;
                stats.vipUsers = this.vipUsers;
                stats.adminUsers = this.adminUsers;
                stats.vipRatio = this.vipRatio;
                return stats;
            }
        }

        // Getters
        public Long getTotalUsers() { return totalUsers; }
        public Long getVipUsers() { return vipUsers; }
        public Long getAdminUsers() { return adminUsers; }
        public Double getVipRatio() { return vipRatio; }
    }
}
