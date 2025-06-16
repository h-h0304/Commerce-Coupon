package com.commercecoupon.controller;

import com.commercecoupon.dto.request.CategoryCreateRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.CategoryResponse;
import com.commercecoupon.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리 API", description = "상품 카테고리 관련 기능")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 모든 활성 카테고리 조회
     */
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "활성화된 모든 카테고리를 조회합니다")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        log.info("카테고리 목록 조회 요청");

        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("카테고리 목록 조회 성공", categories));
    }

    /**
     * 카테고리 상세 조회
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "카테고리 상세 조회", description = "카테고리 ID로 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId) {

        log.info("카테고리 상세 조회 요청: categoryId={}", categoryId);

        CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 조회 성공", category));
    }

    // =============== 관리자 전용 기능 ===============

    /**
     * 카테고리 생성 (관리자 전용)
     */
    @PostMapping
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다 (관리자 전용)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {

        log.info("카테고리 생성 요청: name={}", request.getName());

        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("카테고리 생성 성공", category));
    }

    /**
     * 카테고리 수정 (관리자 전용)
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정합니다 (관리자 전용)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
            @Valid @RequestBody CategoryCreateRequest request) {

        log.info("카테고리 수정 요청: categoryId={}, name={}", categoryId, request.getName());

        CategoryResponse category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", category));
    }

    /**
     * 카테고리 비활성화 (관리자 전용)
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 비활성화", description = "카테고리를 비활성화합니다 (관리자 전용)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId) {

        log.info("카테고리 비활성화 요청: categoryId={}", categoryId);

        categoryService.deactivateCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리가 비활성화되었습니다"));
    }
}