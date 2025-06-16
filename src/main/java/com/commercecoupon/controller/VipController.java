package com.commercecoupon.controller;

import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.service.VipBusinessLogicService;
import com.commercecoupon.service.VipBusinessLogicService.VipBenefitInfo;
import com.commercecoupon.service.VipBusinessLogicService.VipStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * VIP 관련 기능을 제공하는 컨트롤러
 * 🔥 마이그레이션 없이 동작 - 기존 데이터와 호환되는 안전한 VIP 기능
 */
@Slf4j
@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
@Tag(name = "VIP API", description = "VIP 회원 관련 기능")
public class VipController {

    private final VipBusinessLogicService vipBusinessLogicService;

    /**
     * VIP 승격 자격 확인
     */
    @GetMapping("/eligibility")
    @Operation(summary = "VIP 승격 자격 확인", description = "현재 사용자의 VIP 승격 자격을 확인합니다")
    public ResponseEntity<ApiResponse<Boolean>> checkVipEligibility(Authentication authentication) {
        log.info("VIP 승격 자격 확인 요청: email={}", authentication.getName());

        try {
            boolean eligible = vipBusinessLogicService.checkVipEligibility(authentication.getName());
            String message = eligible ? "VIP 승격 자격을 만족합니다" : "VIP 승격 자격을 만족하지 않습니다";

            return ResponseEntity.ok(ApiResponse.success(message, eligible));
        } catch (Exception e) {
            log.error("VIP 승격 자격 확인 중 오류: email={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("VIP 승격 자격 확인 중 오류가 발생했습니다", false));
        }
    }

    /**
     * VIP 승격 신청
     * 🔥 VIP enum이 없으면 안전하게 에러 메시지 반환
     */
    @PostMapping("/promotion")
    @Operation(summary = "VIP 승격", description = "사용자를 VIP로 승격시킵니다")
    public ResponseEntity<ApiResponse<String>> promoteToVip(Authentication authentication) {
        log.info("VIP 승격 신청: email={}", authentication.getName());

        try {
            vipBusinessLogicService.promoteToVip(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("VIP 승격이 완료되었습니다! 축하 쿠폰이 발급되었습니다."));
        } catch (Exception e) {
            log.warn("VIP 승격 실패: email={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * VIP 혜택 정보 조회
     * 🔥 VIP가 아니어도 안전하게 처리 (ADMIN은 VIP 혜택 적용)
     */
    @GetMapping("/benefits")
    @Operation(summary = "VIP 혜택 정보 조회", description = "VIP 회원의 혜택 정보를 조회합니다")
    public ResponseEntity<ApiResponse<VipBenefitInfo>> getVipBenefits(Authentication authentication) {
        log.info("VIP 혜택 정보 조회: email={}", authentication.getName());

        try {
            // VIP 회원 여부 먼저 확인
            if (!vipBusinessLogicService.isVipMember(authentication.getName())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("VIP 회원만 이용할 수 있는 기능입니다")
                );
            }

            VipBenefitInfo benefits = vipBusinessLogicService.getVipBenefitInfo(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("VIP 혜택 정보 조회 성공", benefits));
        } catch (Exception e) {
            log.error("VIP 혜택 조회 중 오류: email={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * VIP 할인 금액 계산 (결제 시 사용)
     * 🔥 VIP가 아니어도 안전하게 0원 반환
     */
    @GetMapping("/discount")
    @Operation(summary = "VIP 할인 금액 계산", description = "주어진 금액에 대한 VIP 할인을 계산합니다")
    public ResponseEntity<ApiResponse<Integer>> calculateVipDiscount(
            @RequestParam Integer amount,
            Authentication authentication) {

        log.info("VIP 할인 계산: email={}, amount={}", authentication.getName(), amount);

        try {
            Integer discount = vipBusinessLogicService.calculateVipDiscount(authentication.getName(), amount);
            String message = discount > 0 ?
                    String.format("VIP 할인 %d원이 적용됩니다", discount) :
                    "VIP 할인이 적용되지 않습니다";

            return ResponseEntity.ok(ApiResponse.success(message, discount));
        } catch (Exception e) {
            log.error("VIP 할인 계산 중 오류: email={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("VIP 할인 계산 중 오류가 발생했습니다", 0));
        }
    }

    /**
     * VIP 회원 여부 확인
     * 🔥 새로 추가: 클라이언트에서 VIP 여부를 확인할 수 있는 간단한 API
     */
    @GetMapping("/status")
    @Operation(summary = "VIP 회원 여부 확인", description = "현재 사용자가 VIP 회원인지 확인합니다")
    public ResponseEntity<ApiResponse<Boolean>> checkVipStatus(Authentication authentication) {
        log.info("VIP 회원 여부 확인: email={}", authentication.getName());

        try {
            boolean isVip = vipBusinessLogicService.isVipMember(authentication.getName());
            String message = isVip ? "VIP 회원입니다" : "일반 회원입니다";

            return ResponseEntity.ok(ApiResponse.success(message, isVip));
        } catch (Exception e) {
            log.error("VIP 상태 확인 중 오류: email={}, error={}", authentication.getName(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("VIP 상태 확인 중 오류가 발생했습니다", false));
        }
    }

    /**
     * VIP 생일 쿠폰 발급 (관리자용)
     * 🔥 ADMIN만 접근 가능 (VIP enum 없어도 동작)
     */
    @PostMapping("/birthday-coupon")
    @Operation(summary = "VIP 생일 쿠폰 발급", description = "VIP 회원에게 생일 쿠폰을 발급합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> issueBirthdayCoupon(
            @RequestParam String email) {

        log.info("VIP 생일 쿠폰 발급 요청: targetEmail={}", email);

        try {
            vipBusinessLogicService.issueBirthdayCouponForVip(email);
            return ResponseEntity.ok(ApiResponse.success("VIP 생일 쿠폰이 발급되었습니다"));
        } catch (Exception e) {
            log.error("VIP 생일 쿠폰 발급 실패: email={}, error={}", email, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * VIP 회원 통계 (관리자용)
     * 🔥 VIP enum 없어도 안전하게 동작
     */
    @GetMapping("/statistics")
    @Operation(summary = "VIP 회원 통계", description = "VIP 회원 관련 통계를 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VipStatistics>> getVipStatistics() {
        log.info("VIP 회원 통계 조회");

        try {
            VipStatistics statistics = vipBusinessLogicService.getVipStatistics();
            return ResponseEntity.ok(ApiResponse.success("VIP 통계 조회 성공", statistics));
        } catch (Exception e) {
            log.error("VIP 통계 조회 중 오류: error={}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VIP 통계 조회 중 오류가 발생했습니다"));
        }
    }
}