package com.commercecoupon.service;

import com.commercecoupon.dto.request.OrderCreateRequest;
import com.commercecoupon.dto.response.*;
import com.commercecoupon.entity.*;
import com.commercecoupon.enums.OrderStatus;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final VipBusinessLogicService vipBusinessLogicService;
    private final CouponService couponService;

    @Transactional
    public OrderDetailResponse createOrder(String userEmail, OrderCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("장바구니가 비어있습니다"));

        if (cart.getItems().isEmpty()) {
            throw new CustomException("장바구니가 비어있습니다");
        }

        validateStock(cart.getItems());

        Coupon usedCoupon = null;
        Integer couponDiscount = 0;

        if (request.getCouponId() != null) {
            usedCoupon = couponRepository.findById(request.getCouponId())
                    .orElseThrow(() -> new CustomException("존재하지 않는 쿠폰입니다"));

            if (!couponService.validateCoupon(request.getCouponId(), userEmail)) {
                throw new CustomException("사용할 수 없는 쿠폰입니다");
            }

            couponDiscount = usedCoupon.getDiscountAmount() != null ?
                    usedCoupon.getDiscountAmount() : 0;
        }

        Integer originalAmount = cart.getTotalAmount();
        Integer vipDiscount = vipBusinessLogicService.calculateVipDiscount(userEmail, originalAmount);
        Integer finalAmount = Math.max(0, originalAmount - couponDiscount - vipDiscount);

        Order order = Order.builder()
                .orderNumber(generateUniqueOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .originalAmount(originalAmount)
                .couponDiscountAmount(couponDiscount)
                .vipDiscountAmount(vipDiscount)
                .finalAmount(finalAmount)
                .usedCoupon(usedCoupon)
                .recipientName(request.getDeliveryInfo().getRecipientName())
                .phone(request.getDeliveryInfo().getPhone())
                .address(request.getDeliveryInfo().getAddress())
                .detailAddress(request.getDeliveryInfo().getDetailAddress())
                .zipCode(request.getDeliveryInfo().getZipCode())
                .deliveryMemo(request.getDeliveryInfo().getDeliveryMemo())
                .memo(request.getMemo())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.fromCartItem(cartItem);
            savedOrder.addItem(orderItem);
            orderItemRepository.save(orderItem);

            Product product = cartItem.getProduct();
            product.decreaseStock(cartItem.getQuantity());
            product.increaseSalesCount(cartItem.getQuantity());
            productRepository.save(product);
        }

        if (usedCoupon != null) {
            couponService.useCoupon(request.getCouponId(), userEmail);
        }

        cart.clearItems();
        cartItemRepository.deleteByCart(cart);
        cartRepository.save(cart);

        return convertToOrderDetailResponse(savedOrder);
    }

    public OrderPageResponse getMyOrders(String userEmail, Integer page, Integer size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<OrderSummaryResponse> orderSummaries = orderPage.getContent().stream()
                .map(this::convertToOrderSummaryResponse)
                .collect(Collectors.toList());

        return OrderPageResponse.builder()
                .orders(orderSummaries)
                .currentPage(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isFirst(orderPage.isFirst())
                .isLast(orderPage.isLast())
                .build();
    }

    public OrderDetailResponse getOrderDetail(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new CustomException("주문을 찾을 수 없거나 접근 권한이 없습니다"));

        return convertToOrderDetailResponse(order);
    }

    @Transactional
    public void cancelOrder(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new CustomException("주문을 찾을 수 없거나 접근 권한이 없습니다"));

        if (!order.isCancellable()) {
            throw new CustomException("취소할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            product.increaseStock(orderItem.getQuantity());
            productRepository.save(product);
        }

        if (order.getUsedCoupon() != null) {
            Coupon coupon = order.getUsedCoupon();
            coupon.setIsUsed(false);
            couponRepository.save(coupon);
        }

        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public OrderPageResponse getAllOrdersForAdmin(OrderStatus status, String keyword,
                                                  Integer page, Integer size,
                                                  String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage;

        if (status != null && keyword != null && !keyword.trim().isEmpty()) {
            orderPage = orderRepository.findByStatusAndKeyword(status, keyword, pageable);
        } else if (status != null) {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            orderPage = orderRepository.findByKeyword(keyword, pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<OrderSummaryResponse> orderSummaries = orderPage.getContent().stream()
                .map(this::convertToOrderSummaryResponse)
                .collect(Collectors.toList());

        return OrderPageResponse.builder()
                .orders(orderSummaries)
                .currentPage(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isFirst(orderPage.isFirst())
                .isLast(orderPage.isLast())
                .build();
    }

    @Transactional
    public OrderDetailResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("존재하지 않는 주문입니다"));

        order.updateStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        return convertToOrderDetailResponse(savedOrder);
    }

    public Object getOrderStatistics(String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period.toLowerCase()) {
            case "day": startDate = endDate.minusDays(1); break;
            case "week": startDate = endDate.minusDays(7); break;
            default: startDate = endDate.minusMonths(1); break;
        }

        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        Long totalSales = orderRepository.sumFinalAmountByDateRange(startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("period", period);
        statistics.put("totalOrders", orders.size());
        statistics.put("totalSales", totalSales != null ? totalSales : 0L);

        return statistics;
    }

    public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("존재하지 않는 주문입니다"));

        return convertToOrderDetailResponse(order);
    }

    // Private Methods

    private void validateStock(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            if (!cartItem.isStockAvailable()) {
                throw new CustomException(
                        String.format("재고가 부족합니다. 상품: %s, 요청 수량: %d, 현재 재고: %d",
                                cartItem.getProduct().getName(),
                                cartItem.getQuantity(),
                                cartItem.getProduct().getStock())
                );
            }
        }
    }

    private String generateUniqueOrderNumber() {
        String orderNumber;
        do {
            orderNumber = Order.generateOrderNumber();
        } while (orderRepository.findByOrderNumber(orderNumber).isPresent());

        return orderNumber;
    }

    private OrderDetailResponse convertToOrderDetailResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());

        OrderDetailResponse.DeliveryInfo deliveryInfo = OrderDetailResponse.DeliveryInfo.builder()
                .recipientName(order.getRecipientName())
                .phone(order.getPhone())
                .address(order.getAddress())
                .detailAddress(order.getDetailAddress())
                .zipCode(order.getZipCode())
                .deliveryMemo(order.getDeliveryMemo())
                .build();

        OrderDetailResponse.OrderUserInfo userInfo = OrderDetailResponse.OrderUserInfo.builder()
                .id(order.getUser().getId())
                .email(order.getUser().getEmail())
                .name(order.getUser().getName())
                .build();

        OrderDetailResponse.UsedCouponInfo couponInfo = null;
        if (order.getUsedCoupon() != null) {
            couponInfo = OrderDetailResponse.UsedCouponInfo.builder()
                    .id(order.getUsedCoupon().getId())
                    .name(order.getUsedCoupon().getName())
                    .discountAmount(order.getCouponDiscountAmount())
                    .build();
        }

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .user(userInfo)
                .items(itemResponses)
                .originalAmount(order.getOriginalAmount())
                .couponDiscountAmount(order.getCouponDiscountAmount())
                .vipDiscountAmount(order.getVipDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .usedCoupon(couponInfo)
                .deliveryInfo(deliveryInfo)
                .memo(order.getMemo())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse convertToOrderSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .userName(order.getUser().getName())
                .itemCount(order.getTotalItemCount())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        Product product = orderItem.getProduct();

        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(orderItem.getProductName())
                .price(orderItem.getUnitPrice())
                .stock(product.getStock())
                .imageUrl(orderItem.getProductImageUrl())
                .category(convertToCategoryResponse(product.getCategory()))
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .salesCount(product.getSalesCount())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .build();

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .product(productResponse)
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(0L)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}