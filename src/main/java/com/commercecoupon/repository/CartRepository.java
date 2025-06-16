package com.commercecoupon.repository;

import com.commercecoupon.entity.Cart;
import com.commercecoupon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 사용자별 장바구니 조회
     */
    Optional<Cart> findByUser(User user);

    /**
     * 사용자 ID로 장바구니 조회
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * 사용자 이메일로 장바구니 조회
     */
    @Query("SELECT c FROM Cart c WHERE c.user.email = :email")
    Optional<Cart> findByUserEmail(@Param("email") String email);

    /**
     * 장바구니 존재 여부 확인
     */
    boolean existsByUser(User user);

    /**
     * 사용자 ID로 장바구니 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}