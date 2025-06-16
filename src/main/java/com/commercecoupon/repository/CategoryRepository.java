package com.commercecoupon.repository;

import com.commercecoupon.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 활성화된 카테고리만 조회 (표시 순서대로)
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 이름으로 카테고리 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리명 중복 확인
     */
    boolean existsByName(String name);

    /**
     * 카테고리별 상품 수 조회
     */
    @Query("SELECT c.id, c.name, COUNT(p) FROM Category c " +
            "LEFT JOIN c.products p WHERE p.status = 'ACTIVE' " +
            "GROUP BY c.id, c.name ORDER BY c.displayOrder")
    List<Object[]> findCategoryWithProductCount();
}