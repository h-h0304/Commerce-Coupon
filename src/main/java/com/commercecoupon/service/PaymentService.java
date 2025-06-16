package com.commercecoupon.service;

import com.commercecoupon.dto.request.PaymentPrepareRequest;
import com.commercecoupon.dto.request.PaymentCompleteRequest;
import com.commercecoupon.dto.response.PaymentPrepareResponse;
import com.commercecoupon.dto.response.PaymentCompleteResponse;
import com.commercecoupon.entity.*;
import com.commercecoupon.enums.PaymentStatus;
import com.commercecoupon.enums.OrderStatus;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentPrepareResponse preparePayment(String userEmail, PaymentPrepareRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Order order = orderRepository.findByIdAndUser(request.getOrderId(), user)
                .orElseThrow(() -> new CustomException("주문을 찾을 수 없거나 접근 권한이 없습니다"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException("결제할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        // 기존 결제가 있는지 확인
        Optional<Payment> existingPayment = paymentRepository.findByOrder(order);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.COMPLETED) {
            throw new CustomException("이미 결제가 완료된 주문입니다");
        }

        // 결제 금액 검증
        if (!request.getAmount().equals(order.getFinalAmount())) {
            throw new CustomException("결제 금액이 일치하지 않습니다");
        }

        String paymentKey = generateUniquePaymentKey(order.getId());

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .user(user)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentPrepareResponse.builder()
                .paymentKey(savedPayment.getPaymentKey())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .amount(savedPayment.getAmount())
                .paymentMethod(savedPayment.getPaymentMethod())
                .paymentUrl("https://api.tosspayments.com/v1/payments/" + paymentKey)
                .successUrl("http://localhost:3000/success")
                .failUrl("http://localhost:3000/fail")
                .build();
    }

    @Transactional
    public PaymentCompleteResponse completePayment(String userEmail, PaymentCompleteRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Payment payment = paymentRepository.findByPaymentKey(request.getPaymentKey())
                .orElseThrow(() -> new CustomException("존재하지 않는 결제입니다"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new CustomException("결제에 접근 권한이 없습니다");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException("결제할 수 없는 상태입니다: " + payment.getStatus());
        }

        if (!payment.getAmount().equals(request.getAmount())) {
            throw new CustomException("결제 금액이 일치하지 않습니다");
        }

        // 실제 PG사 연동은 여기서 처리 (토스페이먼츠, 포트원 등)
        // 현재는 시뮬레이션으로 처리
        String pgTransactionId = "PG" + System.currentTimeMillis();
        String cardInfo = "신한카드(*1234)"; // 실제로는 PG사에서 받아옴

        payment.completePayment(pgTransactionId, cardInfo);
        Payment savedPayment = paymentRepository.save(payment);

        // 주문 상태를 PAID로 변경
        Order order = payment.getOrder();
        order.updateStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return convertToPaymentCompleteResponse(savedPayment);
    }

    @Transactional
    public void cancelPayment(String userEmail, Long paymentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Payment payment = paymentRepository.findByIdAndUser(paymentId, user)
                .orElseThrow(() -> new CustomException("결제를 찾을 수 없거나 접근 권한이 없습니다"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new CustomException("취소할 수 없는 결제 상태입니다: " + payment.getStatus());
        }

        // 실제 PG사 취소 API 호출은 여기서 처리
        payment.cancelPayment();
        paymentRepository.save(payment);

        // 연관된 주문도 취소 처리
        Order order = payment.getOrder();
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public PaymentCompleteResponse getPaymentDetail(String userEmail, Long paymentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다"));

        Payment payment = paymentRepository.findByIdAndUser(paymentId, user)
                .orElseThrow(() -> new CustomException("결제를 찾을 수 없거나 접근 권한이 없습니다"));

        return convertToPaymentCompleteResponse(payment);
    }

    // Private Methods

    private String generateUniquePaymentKey(Long orderId) {
        String paymentKey;
        do {
            paymentKey = Payment.generatePaymentKey(orderId);
        } while (paymentRepository.existsByPaymentKey(paymentKey));

        return paymentKey;
    }

    private PaymentCompleteResponse convertToPaymentCompleteResponse(Payment payment) {
        return PaymentCompleteResponse.builder()
                .paymentId(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .approvedAt(payment.getApprovedAt())
                .pgTransactionId(payment.getPgTransactionId())
                .cardInfo(payment.getCardInfo())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}