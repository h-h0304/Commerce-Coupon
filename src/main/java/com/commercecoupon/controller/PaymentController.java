package com.commercecoupon.controller;

import com.commercecoupon.dto.request.PaymentPrepareRequest;
import com.commercecoupon.dto.request.PaymentCompleteRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.PaymentPrepareResponse;
import com.commercecoupon.dto.response.PaymentCompleteResponse;
import com.commercecoupon.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "결제 관련 기능")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비
     */
    @PostMapping("/prepare")
    @Operation(summary = "결제 준비", description = "주문에 대한 결제를 준비합니다")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
            @Valid @RequestBody PaymentPrepareRequest request,
            Authentication authentication) {

        log.info("결제 준비 요청: email={}, orderId={}, paymentMethod={}",
                authentication.getName(), request.getOrderId(), request.getPaymentMethod());

        PaymentPrepareResponse response = paymentService.preparePayment(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("결제 준비가 완료되었습니다", response));
    }

    /**
     * 결제 완료
     */
    @PostMapping("/complete")
    @Operation(summary = "결제 완료", description = "결제를 완료하고 주문을 확정합니다")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> completePayment(
            @Valid @RequestBody PaymentCompleteRequest request,
            Authentication authentication) {

        log.info("결제 완료 요청: email={}, paymentKey={}",
                authentication.getName(), request.getPaymentKey());

        PaymentCompleteResponse response = paymentService.completePayment(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("결제가 완료되었습니다", response));
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "결제 취소", description = "결제를 취소하고 환불을 진행합니다")
    public ResponseEntity<ApiResponse<String>> cancelPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        log.info("결제 취소 요청: email={}, paymentId={}", authentication.getName(), paymentId);

        paymentService.cancelPayment(authentication.getName(), paymentId);
        return ResponseEntity.ok(ApiResponse.success("결제가 취소되었습니다"));
    }

    /**
     * 결제 내역 조회
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 내역 조회", description = "특정 결제의 상세 내역을 조회합니다")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> getPaymentDetail(
            @PathVariable Long paymentId,
            Authentication authentication) {

        log.info("결제 내역 조회 요청: email={}, paymentId={}", authentication.getName(), paymentId);

        PaymentCompleteResponse response = paymentService.getPaymentDetail(authentication.getName(), paymentId);
        return ResponseEntity.ok(ApiResponse.success("결제 내역 조회 성공", response));
    }
}