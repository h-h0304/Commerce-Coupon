package com.commercecoupon.service;

import com.commercecoupon.repository.ProductRepository;
import com.commercecoupon.dto.request.CategoryCreateRequest;
import com.commercecoupon.dto.response.CategoryResponse;
import com.commercecoupon.entity.Category;
import com.commercecoupon.enums.ProductStatus;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * 모든 활성 카테고리 조회
     */
    public List<CategoryResponse> getAllActiveCategories() {
        log.debug("활성 카테고리 목록 조회");

        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 ID로 조회
     */
    public CategoryResponse getCategoryById(Long categoryId) {
        log.debug("카테고리 조회: categoryId={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다"));

        return convertToCategoryResponse(category);
    }

    /**
     * 카테고리 생성 (관리자용)
     */
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("카테고리 생성: name={}", request.getName());

        // 카테고리명 중복 확인
        if (categoryRepository.existsByName(request.getName())) {
            throw new CustomException("이미 존재하는 카테고리명입니다");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: categoryId={}", savedCategory.getId());

        return convertToCategoryResponse(savedCategory);
    }

    /**
     * 카테고리 수정 (관리자용)
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryCreateRequest request) {
        log.info("카테고리 수정: categoryId={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다"));

        // 다른 카테고리에서 같은 이름 사용 여부 확인
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new CustomException("이미 존재하는 카테고리명입니다");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 수정 완료: categoryId={}", savedCategory.getId());

        return convertToCategoryResponse(savedCategory);
    }

    /**
     * 카테고리 비활성화 (관리자용)
     */
    @Transactional
    public void deactivateCategory(Long categoryId) {
        log.info("카테고리 비활성화: categoryId={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다"));

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("카테고리 비활성화 완료: categoryId={}", categoryId);
    }

    /**
     * DTO 변환 메서드
     */
    private CategoryResponse convertToCategoryResponse(Category category) {
        // ProductRepository에서 카테고리별 상품 수 조회
        Long productCount = productRepository.countByCategoryIdAndStatus(
                category.getId(), ProductStatus.ACTIVE);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(productCount)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}