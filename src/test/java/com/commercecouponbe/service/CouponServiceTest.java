package com.commercecouponbe.service;

import com.commercecoupon.dto.response.CouponResponse;
import com.commercecoupon.entity.Coupon;
import com.commercecoupon.entity.User;
import com.commercecoupon.enums.CouponType;
import com.commercecoupon.enums.Role;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.CouponRepository;
import com.commercecoupon.repository.UserRepository;
import com.commercecoupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("웰컴 쿠폰 발급 테스트")
    void issueWelcomeCoupon_Success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트")
                .role(Role.USER)
                .build();

        when(couponRepository.findByUserEmailAndType("test@example.com", CouponType.WELCOME))
                .thenReturn(List.of()); // 기존 웰컴 쿠폰 없음

        Coupon savedCoupon = Coupon.builder()
                .id(1L)
                .name("웰컴 쿠폰")
                .type(CouponType.WELCOME)
                .discountAmount(BigDecimal.valueOf(5000))
                .expiryDate(LocalDateTime.now().plusDays(30))
                .user(user)
                .build();

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // when
        couponService.issueWelcomeCoupon(user);

        // then
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("사용자 쿠폰 목록 조회 테스트")
    void getUserCoupons_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .name("테스트")
                .role(Role.USER)
                .build();

        Coupon coupon = Coupon.builder()
                .id(1L)
                .name("웰컴 쿠폰")
                .type(CouponType.WELCOME)
                .discountAmount(BigDecimal.valueOf(5000))
                .expiryDate(LocalDateTime.now().plusDays(30))
                .user(user)
                .isUsed(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(couponRepository.findByUserEmailOrderByCreatedAtDesc(email)).thenReturn(List.of(coupon));

        // when
        List<CouponResponse> result = couponService.getUserCoupons(email);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("웰컴 쿠폰");
        assertThat(result.get(0).getDiscountAmount()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(result.get(0).isUsed()).isFalse();
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 사용 성공 테스트")
    void useCoupon_Success() {
        // given
        String email = "test@example.com";
        Long couponId = 1L;

        User user = User.builder()
                .id(1L)
                .email(email)
                .name("테스트")
                .role(Role.USER)
                .build();

        Coupon coupon = Coupon.builder()
                .id(couponId)
                .name("웰컴 쿠폰")
                .type(CouponType.WELCOME)
                .discountAmount(BigDecimal.valueOf(5000))
                .expiryDate(LocalDateTime.now().plusDays(30))
                .user(user)
                .isUsed(false)
                .isActive(true)
                .build();

        when(couponRepository.findByIdAndUserEmail(couponId, email)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // when
        CouponResponse result = couponService.useCoupon(email, couponId);

        // then
        assertThat(result.isUsed()).isTrue();
        assertThat(result.getUsedAt()).isNotNull();
        verify(couponRepository).save(coupon);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 사용 시 예외 발생")
    void useCoupon_CouponNotFound_ThrowsException() {
        // given
        String email = "test@example.com";
        Long couponId = 999L;

        when(couponRepository.findByIdAndUserEmail(couponId, email)).thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("쿠폰을 찾을 수 없습니다");

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(email, couponId))
                .isInstanceOf(CustomException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다");
    }
}