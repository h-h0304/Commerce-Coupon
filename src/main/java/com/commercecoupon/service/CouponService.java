package com.commercecoupon.service;

import com.commercecoupon.dto.response.CouponResponse;
import com.commercecoupon.entity.Coupon;
import com.commercecoupon.entity.User;
import com.commercecoupon.enums.CouponType;
import com.commercecoupon.enums.Role;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.CouponRepository;
import com.commercecoupon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    /**
     * 🆕 웰컴 쿠폰 자동 발급 (User 객체를 직접 받도록 수정)
     * 🔥 마이그레이션 없이 동작 - 기존 USER/ADMIN만 고려, VIP는 옵션
     */
    public void issueWelcomeCoupon(User user) {
        if (user == null || user.getId() == null) {
            log.error("웰컴 쿠폰 발급 실패 - 유효하지 않은 사용자: {}", user);
            throw new IllegalArgumentException("유효하지 않은 사용자입니다");
        }

        log.info("웰컴 쿠폰 발급 시작: userId={}, email={}", user.getId(), user.getEmail());

        try {
            // 이미 웰컴 쿠폰을 발급받았는지 확인
            boolean hasWelcomeCoupon = couponRepository.existsByUserIdAndType(user.getId(), CouponType.WELCOME);
            if (hasWelcomeCoupon) {
                log.warn("이미 웰컴 쿠폰이 발급된 사용자: userId={}", user.getId());
                return; // 중복 발급 방지
            }

            // 🔥 사용자 등급에 따른 웰컴 쿠폰 차등 발급 (안전한 방식)
            Coupon welcomeCoupon = createWelcomeCouponByUserRole(user);

            Coupon savedCoupon = couponRepository.save(welcomeCoupon);
            log.info("웰컴 쿠폰 발급 완료: couponId={}, userId={}, discountAmount={}",
                    savedCoupon.getId(), user.getId(), savedCoupon.getDiscountAmount());

        } catch (Exception e) {
            log.error("웰컴 쿠폰 발급 중 오류 발생: userId={}, error={}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("웰컴 쿠폰 발급 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 🆕 사용자 등급에 따른 웰컴 쿠폰 생성 (마이그레이션 없이 안전하게 동작)
     */
    private Coupon createWelcomeCouponByUserRole(User user) {
        String couponName;
        Integer discountAmount;
        Integer validityDays;

        // 🔥 기존 데이터 호환: VIP가 없는 기존 DB에서도 안전하게 동작
        try {
            // VIP enum이 존재하는지 확인
            Role vipRole = Role.VIP;

            // VIP enum이 존재하는 경우의 로직
            switch (user.getRole()) {
                case VIP:
                    couponName = "VIP 웰컴 쿠폰";
                    discountAmount = 10000; // VIP는 10,000원
                    validityDays = 60; // VIP는 60일 유효
                    log.info("VIP 웰컴 쿠폰 생성: userId={}, amount={}", user.getId(), discountAmount);
                    break;
                case ADMIN:
                    couponName = "관리자 웰컴 쿠폰";
                    discountAmount = 15000; // 관리자는 15,000원
                    validityDays = 90; // 관리자는 90일 유효
                    log.info("관리자 웰컴 쿠폰 생성: userId={}, amount={}", user.getId(), discountAmount);
                    break;
                case USER:
                default:
                    couponName = "웰컴 쿠폰";
                    discountAmount = 5000; // 일반 회원은 5,000원
                    validityDays = 30; // 일반 회원은 30일 유효
                    log.info("일반 웰컴 쿠폰 생성: userId={}, amount={}", user.getId(), discountAmount);
                    break;
            }
        } catch (Exception e) {
            // VIP enum이 없는 경우 기존 USER/ADMIN만 처리
            log.debug("VIP Role 미정의, 기존 USER/ADMIN만 처리: userId={}, role={}", user.getId(), user.getRole());

            if (user.getRole() == Role.ADMIN) {
                couponName = "관리자 웰컴 쿠폰";
                discountAmount = 15000; // 관리자는 15,000원
                validityDays = 90; // 관리자는 90일 유효
                log.info("관리자 웰컴 쿠폰 생성: userId={}, amount={}", user.getId(), discountAmount);
            } else {
                // USER 또는 기타
                couponName = "웰컴 쿠폰";
                discountAmount = 5000; // 일반 회원은 5,000원
                validityDays = 30; // 일반 회원은 30일 유효
                log.info("일반 웰컴 쿠폰 생성: userId={}, amount={}", user.getId(), discountAmount);
            }
        }

        return Coupon.builder()
                .name(couponName)
                .type(CouponType.WELCOME)
                .discountAmount(discountAmount)
                .discountPercent(null) // 웰컴 쿠폰은 정액 할인
                .expiryDate(LocalDateTime.now().plusDays(validityDays))
                .isUsed(false)
                .user(user)
                .build();
    }

    /**
     * 🆕 VIP 전용 쿠폰 발급 (안전한 방식)
     */
    public void issueVipSpecialCoupon(Long userId, String couponName, Integer discountAmount, Integer validityDays) {
        log.info("VIP 전용 쿠폰 발급 시작: userId={}, couponName={}", userId, couponName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("VIP 쿠폰 발급 실패 - 사용자 없음: userId={}", userId);
                    return new CustomException("존재하지 않는 사용자입니다");
                });

        // 🔥 VIP 회원 여부 확인 (안전한 방식)
        boolean isVipEligible = false;
        try {
            isVipEligible = (user.getRole() == Role.VIP || user.getRole() == Role.ADMIN);
        } catch (Exception e) {
            // VIP enum이 없는 경우 ADMIN만 VIP 쿠폰 받을 수 있음
            log.debug("VIP Role 미정의, ADMIN만 VIP 쿠폰 발급 가능");
            isVipEligible = (user.getRole() == Role.ADMIN);
        }

        if (!isVipEligible) {
            log.warn("VIP 쿠폰 발급 실패 - VIP 회원 아님: userId={}, role={}", userId, user.getRole());
            throw new CustomException("VIP 회원만 발급받을 수 있는 쿠폰입니다");
        }

        try {
            Coupon vipCoupon = Coupon.builder()
                    .name(couponName)
                    .type(CouponType.SPECIAL) // VIP 전용은 SPECIAL 타입
                    .discountAmount(discountAmount)
                    .discountPercent(null)
                    .expiryDate(LocalDateTime.now().plusDays(validityDays))
                    .isUsed(false)
                    .user(user)
                    .build();

            Coupon savedCoupon = couponRepository.save(vipCoupon);
            log.info("VIP 전용 쿠폰 발급 완료: couponId={}, userId={}", savedCoupon.getId(), userId);

        } catch (Exception e) {
            log.error("VIP 쿠폰 발급 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("VIP 쿠폰 발급 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 🆕 VIP 할인 정책 계산 (안전한 방식)
     */
    public Integer calculateVipDiscount(User user, Integer originalAmount) {
        try {
            // VIP 회원 여부 확인 (안전한 방식)
            boolean isVip = false;
            try {
                isVip = (user.getRole() == Role.VIP || user.getRole() == Role.ADMIN);
            } catch (Exception e) {
                // VIP enum이 없는 경우 ADMIN만 VIP 혜택
                log.debug("VIP Role 미정의, ADMIN만 VIP 혜택 적용");
                isVip = (user.getRole() == Role.ADMIN);
            }

            if (!isVip) {
                return 0; // VIP가 아니면 추가 할인 없음
            }

            // VIP 추가 할인 정책: 구매 금액의 5% 추가 할인 (최대 5,000원)
            Integer vipDiscountRate = 5; // 5%
            Integer maxVipDiscount = 5000; // 최대 5,000원

            Integer vipDiscount = Math.min(originalAmount * vipDiscountRate / 100, maxVipDiscount);

            log.info("VIP 추가 할인 계산: userId={}, originalAmount={}, vipDiscount={}",
                    user.getId(), originalAmount, vipDiscount);

            return vipDiscount;

        } catch (Exception e) {
            log.error("VIP 할인 계산 중 오류: userId={}, error={}", user.getId(), e.getMessage());
            return 0; // 오류 시 할인 없음으로 안전하게 처리
        }
    }

    /**
     * 🆕 쿠폰 유효성 검증
     */
    public boolean validateCoupon(Long couponId, String userEmail) {
        log.debug("쿠폰 유효성 검증 시작: couponId={}, userEmail={}", couponId, userEmail);

        try {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CustomException("존재하지 않는 쿠폰입니다"));

            // 1. 쿠폰 소유자 확인
            if (!coupon.getUser().getEmail().equals(userEmail)) {
                log.warn("쿠폰 소유자 불일치: couponId={}, couponOwner={}, requestUser={}",
                        couponId, coupon.getUser().getEmail(), userEmail);
                return false;
            }

            // 2. 이미 사용된 쿠폰인지 확인
            if (Boolean.TRUE.equals(coupon.getIsUsed())) {
                log.warn("이미 사용된 쿠폰: couponId={}", couponId);
                return false;
            }

            // 3. 만료일 확인
            if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("만료된 쿠폰: couponId={}, expiryDate={}", couponId, coupon.getExpiryDate());
                return false;
            }

            log.debug("쿠폰 유효성 검증 성공: couponId={}", couponId);
            return true;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("쿠폰 유효성 검증 중 오류 발생: couponId={}, error={}", couponId, e.getMessage(), e);
            throw new RuntimeException("쿠폰 유효성 검증 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 🆕 쿠폰 사용 처리
     */
    @Transactional
    public void useCoupon(Long couponId, String userEmail) {
        log.info("쿠폰 사용 처리 시작: couponId={}, userEmail={}", couponId, userEmail);

        if (!validateCoupon(couponId, userEmail)) {
            throw new CustomException("사용할 수 없는 쿠폰입니다");
        }

        try {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CustomException("존재하지 않는 쿠폰입니다"));

            coupon.setIsUsed(true);
            couponRepository.save(coupon);

            log.info("쿠폰 사용 처리 완료: couponId={}, userEmail={}", couponId, userEmail);

        } catch (Exception e) {
            log.error("쿠폰 사용 처리 중 오류 발생: couponId={}, error={}", couponId, e.getMessage(), e);
            throw new RuntimeException("쿠폰 사용 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 기존 메서드: 사용자 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getUserCoupons(String email) {
        log.debug("사용자 쿠폰 목록 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        List<Coupon> coupons = couponRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return coupons.stream()
                .map(this::convertToCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🆕 사용 가능한 쿠폰만 조회 (결제 시 사용)
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons(String email) {
        log.debug("사용 가능한 쿠폰 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        List<Coupon> availableCoupons = couponRepository.findAvailableCouponsByUserId(user.getId(), LocalDateTime.now());

        return availableCoupons.stream()
                .map(this::convertToCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * DTO 변환 메서드
     */
    private CouponResponse convertToCouponResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getType(),
                coupon.getDiscountAmount(),
                coupon.getDiscountPercent(),
                coupon.getExpiryDate(),
                coupon.getIsUsed(),
                coupon.getCreatedAt()
        );
    }
}