package com.commercecoupon.service;

import com.commercecoupon.dto.request.CartAddRequest;
import com.commercecoupon.dto.response.CartResponse;
import com.commercecoupon.dto.response.CartItemResponse;
import com.commercecoupon.dto.response.ProductResponse;
import com.commercecoupon.dto.response.CategoryResponse;
import com.commercecoupon.entity.*;
import com.commercecoupon.repository.*;
import com.commercecoupon.exception.CustomException;
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
@Transactional(readOnly = true)
public class CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VipBusinessLogicService vipBusinessLogicService;

    /**
     * 장바구니 조회
     */
    public CartResponse getCart(String userEmail) {
        log.info("장바구니 조회: userEmail={}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Cart cart = getOrCreateCart(user);

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        Integer totalAmount = cart.getTotalAmount();
        Integer vipDiscount = calculateVipDiscount(userEmail, totalAmount);
        Integer finalAmount = totalAmount - vipDiscount;

        return CartResponse.builder()
                .userId(user.getId())
                .items(itemResponses)
                .totalItemCount(cart.getTotalItemCount())
                .totalAmount(totalAmount)
                .expectedVipDiscount(vipDiscount)
                .expectedFinalAmount(finalAmount)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * 장바구니에 상품 추가
     */
    @Transactional
    public CartResponse addToCart(String userEmail, CartAddRequest request) {
        log.info("장바구니 상품 추가: userEmail={}, productId={}, quantity={}",
                userEmail, request.getProductId(), request.getQuantity());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomException("존재하지 않는 상품입니다"));

        Cart cart = getOrCreateCart(user);

        // 기존 장바구니에 같은 상품이 있는지 확인
        CartItem existingItem = cart.findItemByProduct(product);

        if (existingItem != null) {
            // 기존 아이템이 있으면 수량 증가
            Integer newQuantity = existingItem.getQuantity() + request.getQuantity();

            // 재고 확인
            if (product.getStock() < newQuantity) {
                throw new CustomException(
                        String.format("재고가 부족합니다. 요청 수량: %d, 현재 재고: %d, 장바구니 기존 수량: %d",
                                request.getQuantity(), product.getStock(), existingItem.getQuantity())
                );
            }

            existingItem.updateQuantity(newQuantity);
            cartItemRepository.save(existingItem);

            log.info("기존 장바구니 아이템 수량 증가: cartItemId={}, newQuantity={}",
                    existingItem.getId(), newQuantity);
        } else {
            // 새로운 아이템 추가
            // 재고 확인
            if (product.getStock() < request.getQuantity()) {
                throw new CustomException("재고가 부족합니다. 현재 재고: " + product.getStock());
            }

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            cart.addItem(newItem);
            cartItemRepository.save(newItem);

            log.info("새 장바구니 아이템 추가: productId={}, quantity={}",
                    request.getProductId(), request.getQuantity());
        }

        cartRepository.save(cart);

        log.info("장바구니에 상품 추가 완료: productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        return getCart(userEmail);
    }

    /**
     * 장바구니에서 상품 제거
     */
    @Transactional
    public CartResponse removeFromCart(String userEmail, Long cartItemId) {
        log.info("장바구니 상품 삭제: userEmail={}, cartItemId={}", userEmail, cartItemId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException("존재하지 않는 장바구니 아이템입니다"));

        // 소유자 확인
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new CustomException("해당 장바구니 아이템에 접근 권한이 없습니다");
        }

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        log.info("장바구니에서 상품 삭제 완료: cartItemId={}", cartItemId);

        return getCart(userEmail);
    }

    /**
     * 장바구니 전체 비우기
     */
    @Transactional
    public void clearCart(String userEmail) {
        log.info("장바구니 전체 비우기: userEmail={}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart != null) {
            cart.clearItems();
            cartItemRepository.deleteByCart(cart);
            cartRepository.save(cart);

            log.info("장바구니 전체 비우기 완료: userEmail={}, cartId={}", userEmail, cart.getId());
        } else {
            log.info("비울 장바구니가 없습니다: userEmail={}", userEmail);
        }
    }

    /**
     * 장바구니 아이템 수량 업데이트
     */
    @Transactional
    public CartResponse updateCartItemQuantity(String userEmail, Long cartItemId, Integer quantity) {
        log.info("장바구니 수량 변경: userEmail={}, cartItemId={}, quantity={}",
                userEmail, cartItemId, quantity);

        if (quantity < 1) {
            throw new CustomException("수량은 1개 이상이어야 합니다");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException("존재하지 않는 장바구니 아이템입니다"));

        // 소유자 확인
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new CustomException("해당 장바구니 아이템에 접근 권한이 없습니다");
        }

        // 재고 확인
        if (cartItem.getProduct().getStock() < quantity) {
            throw new CustomException("재고가 부족합니다. 현재 재고: " + cartItem.getProduct().getStock());
        }

        cartItem.updateQuantity(quantity);
        cartItemRepository.save(cartItem);

        log.info("장바구니 수량 변경 완료: cartItemId={}, newQuantity={}", cartItemId, quantity);

        return getCart(userEmail);
    }

    /**
     * 재고 부족 아이템 조회
     */
    public List<CartItemResponse> getInsufficientStockItems(String userEmail) {
        log.info("재고 부족 아이템 조회: userEmail={}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return List.of();
        }

        return cart.getItems().stream()
                .filter(item -> !item.isStockAvailable())
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 장바구니 조회 또는 생성
     */
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * CartItem을 CartItemResponse로 변환
     */
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .category(convertToCategoryResponse(product.getCategory()))
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .salesCount(product.getSalesCount())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .build();

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .product(productResponse)
                .quantity(cartItem.getQuantity())
                .unitPrice(product.getPrice())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }

    /**
     * Category를 CategoryResponse로 변환
     */
    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(0L) // 필요시 별도 조회
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * VIP 할인 금액 계산
     */
    private Integer calculateVipDiscount(String userEmail, Integer originalAmount) {
        try {
            return vipBusinessLogicService.calculateVipDiscount(userEmail, originalAmount);
        } catch (Exception e) {
            log.warn("VIP 할인 계산 실패: userEmail={}, error={}", userEmail, e.getMessage());
            return 0;
        }
    }
}