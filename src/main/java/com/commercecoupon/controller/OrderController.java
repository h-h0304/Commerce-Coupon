package com.commercecoupon.controller;

import com.commercecoupon.dto.request.OrderCreateRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.OrderDetailResponse;
import com.commercecoupon.dto.response.OrderPageResponse;
import com.commercecoupon.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문 관련 기능")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping
    @Operation(summary = "주문 생성", description = "장바구니의 상품들로 주문을 생성합니다")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication) {

        log.info("주문 생성 요청: email={}, couponId={}",
                authentication.getName(), request.getCouponId());

        OrderDetailResponse order = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("주문이 생성되었습니다", order));
    }

    /**
     * 내 주문 목록 조회
     */
    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "현재 사용자의 주문 목록을 조회합니다")
    public ResponseEntity<ApiResponse<OrderPageResponse>> getMyOrders(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {

        log.info("주문 목록 조회 요청: email={}, page={}", authentication.getName(), page);

        OrderPageResponse orders = orderService.getMyOrders(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.success("주문 목록 조회 성공", orders));
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId,
            Authentication authentication) {

        log.info("주문 상세 조회 요청: email={}, orderId={}", authentication.getName(), orderId);

        OrderDetailResponse order = orderService.getOrderDetail(authentication.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("주문 상세 조회 성공", order));
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId,
            Authentication authentication) {

        log.info("주문 취소 요청: email={}, orderId={}", authentication.getName(), orderId);

        orderService.cancelOrder(authentication.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다"));
    }
}