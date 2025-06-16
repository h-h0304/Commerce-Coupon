package com.commercecoupon.repository;

import com.commercecoupon.entity.Cart;
import com.commercecoupon.entity.CartItem;
import com.commercecoupon.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 장바구니별 아이템 조회
     */
    List<CartItem> findByCartOrderByCreatedAtDesc(Cart cart);

    /**
     * 장바구니와 상품으로 아이템 조회
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * 장바구니 ID로 아이템 조회
     */
    List<CartItem> findByCartIdOrderByCreatedAtDesc(Long cartId);

    /**
     * 특정 장바구니의 모든 아이템 삭제
     */
    void deleteByCart(Cart cart);

    /**
     * 장바구니 ID로 모든 아이템 삭제
     */
    void deleteByCartId(Long cartId);

    /**
     * 장바구니의 총 아이템 수 조회
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);

    /**
     * 장바구니의 총 수량 조회
     */
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);

    /**
     * 특정 상품이 담긴 모든 장바구니 아이템 조회
     */
    List<CartItem> findByProduct(Product product);

    /**
     * 재고가 부족한 장바구니 아이템 조회
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.quantity > ci.product.stock")
    List<CartItem> findItemsWithInsufficientStock();

    /**
     * 특정 장바구니에서 재고가 부족한 아이템 조회
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.quantity > ci.product.stock")
    List<CartItem> findInsufficientStockItemsByCartId(@Param("cartId") Long cartId);
}