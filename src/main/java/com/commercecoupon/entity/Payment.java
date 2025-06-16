package com.commercecoupon.entity;

import com.commercecoupon.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String paymentKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String pgTransactionId;

    @Column(length = 100)
    private String cardInfo;

    private LocalDateTime approvedAt;

    @Column(length = 1000)
    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 결제 키 생성
     */
    public static String generatePaymentKey(Long orderId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("tgen_%04d%02d%02d%02d%02d%02d_%d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(),
                orderId);
    }

    /**
     * 결제 완료 처리
     */
    public void completePayment(String pgTransactionId, String cardInfo) {
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.cardInfo = cardInfo;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 결제 취소 처리
     */
    public void cancelPayment() {
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * 환불 처리
     */
    public void refundPayment(boolean isPartial) {
        this.status = isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED;
    }
}