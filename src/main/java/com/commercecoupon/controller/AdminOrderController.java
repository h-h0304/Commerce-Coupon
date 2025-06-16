package com.commercecoupon.controller;

import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.OrderDetailResponse;
import com.commercecoupon.dto.response.OrderPageResponse;
import com.commercecoupon.enums.OrderStatus;
import com.commercecoupon.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "관리자 주문 API", description = "관리자용 주문 관리 기능")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 관리자용 주문 목록 조회 (모든 주문)
     */
    @GetMapping
    @Operation(summary = "관리자 주문 목록 조회", description = "모든 주문을 조회합니다")
    public ResponseEntity<ApiResponse<OrderPageResponse>> getAllOrders(
            @Parameter(description = "주문 상태") @RequestParam(required = false) String status,
            @Parameter(description = "검색 키워드 (주문번호, 사용자명)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("관리자 주문 목록 조회: status={}, keyword={}, page={}", status, keyword, page);

        OrderStatus orderStatus = null;
        if (status != null) {
            orderStatus = OrderStatus.valueOf(status);
        }

        OrderPageResponse orders = orderService.getAllOrdersForAdmin(
                orderStatus, keyword, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success("주문 목록 조회 성공", orders));
    }

    /**
     * 주문 상태 변경
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId,
            @Parameter(description = "새로운 주문 상태", example = "SHIPPED") @RequestParam String status) {

        log.info("주문 상태 변경 요청: orderId={}, newStatus={}", orderId, status);

        OrderStatus newStatus = OrderStatus.valueOf(status);
        OrderDetailResponse order = orderService.updateOrderStatus(orderId, newStatus);

        return ResponseEntity.ok(ApiResponse.success("주문 상태가 변경되었습니다", order));
    }

    /**
     * 주문 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "주문 통계 조회", description = "주문 관련 통계를 조회합니다")
    public ResponseEntity<ApiResponse<Object>> getOrderStatistics(
            @Parameter(description = "통계 기간 (일/주/월)") @RequestParam(defaultValue = "month") String period) {

        log.info("주문 통계 조회: period={}", period);

        Object statistics = orderService.getOrderStatistics(period);
        return ResponseEntity.ok(ApiResponse.success("주문 통계 조회 성공", statistics));
    }

    /**
     * 주문 상세 조회 (관리자용)
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회 (관리자)", description = "관리자가 특정 주문의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetailForAdmin(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId) {

        log.info("관리자 주문 상세 조회: orderId={}", orderId);

        OrderDetailResponse order = orderService.getOrderDetailForAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success("주문 상세 조회 성공", order));
    }
}