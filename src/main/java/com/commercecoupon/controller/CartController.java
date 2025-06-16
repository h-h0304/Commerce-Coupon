package com.commercecoupon.controller;

import com.commercecoupon.dto.request.CartAddRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.CartItemResponse;
import com.commercecoupon.dto.response.CartResponse;
import com.commercecoupon.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니 관련 기능")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     */
    @GetMapping
    @Operation(summary = "장바구니 조회", description = "현재 사용자의 장바구니를 조회합니다")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        log.info("장바구니 조회 요청: email={}", authentication.getName());

        CartResponse cart = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("장바구니 조회 성공", cart));
    }

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/items")
    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품을 추가합니다")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody CartAddRequest request,
            Authentication authentication) {

        log.info("장바구니 상품 추가 요청: email={}, productId={}, quantity={}",
                authentication.getName(), request.getProductId(), request.getQuantity());

        CartResponse cart = cartService.addToCart(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("장바구니에 상품이 추가되었습니다", cart));
    }

    /**
     * 장바구니 상품 개별 삭제
     */
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @Parameter(description = "장바구니 아이템 ID", example = "1") @PathVariable Long cartItemId,
            Authentication authentication) {

        log.info("장바구니 상품 삭제 요청: email={}, cartItemId={}",
                authentication.getName(), cartItemId);

        CartResponse cart = cartService.removeFromCart(authentication.getName(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("장바구니에서 상품이 삭제되었습니다", cart));
    }

    /**
     * 장바구니 전체 비우기
     */
    @DeleteMapping
    @Operation(summary = "장바구니 전체 비우기", description = "장바구니의 모든 상품을 삭제합니다")
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        log.info("장바구니 전체 비우기 요청: email={}", authentication.getName());

        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("장바구니가 비워졌습니다"));
    }

    /**
     * 장바구니 상품 수량 변경
     */
    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 상품 수량 변경", description = "장바구니 상품의 수량을 변경합니다")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItemQuantity(
            @Parameter(description = "장바구니 아이템 ID", example = "1") @PathVariable Long cartItemId,
            @Parameter(description = "새로운 수량", example = "3") @RequestParam Integer quantity,
            Authentication authentication) {

        log.info("장바구니 수량 변경 요청: email={}, cartItemId={}, quantity={}",
                authentication.getName(), cartItemId, quantity);

        CartResponse cart = cartService.updateCartItemQuantity(authentication.getName(), cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("장바구니 수량이 변경되었습니다", cart));
    }

    /**
     * 재고 부족 상품 조회
     */
    @GetMapping("/insufficient-stock")
    @Operation(summary = "재고 부족 상품 조회", description = "장바구니에서 재고가 부족한 상품들을 조회합니다")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getInsufficientStockItems(
            Authentication authentication) {

        log.info("재고 부족 상품 조회: email={}", authentication.getName());

        List<CartItemResponse> items = cartService.getInsufficientStockItems(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("재고 부족 상품 조회 성공", items));
    }
}