package com.commercecoupon.controller;

import com.commercecoupon.dto.request.ProductCreateRequest;
import com.commercecoupon.dto.request.ProductSearchRequest;
import com.commercecoupon.dto.request.ProductUpdateRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.ProductDetailResponse;
import com.commercecoupon.dto.response.ProductPageResponse;
import com.commercecoupon.dto.response.ProductResponse;
import com.commercecoupon.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "관리자 상품 API", description = "관리자용 상품 관리 기능")
public class AdminProductController {

    private final ProductService productService;

    /**
     * 관리자용 상품 목록 조회 (모든 상태 포함)
     */
    @GetMapping
    @Operation(summary = "관리자용 상품 목록 조회", description = "모든 상태의 상품을 조회합니다")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getAllProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "최소 가격") @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "상품 상태") @RequestParam(required = false) String status,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size) {

        log.info("관리자용 상품 목록 조회: keyword={}, status={}", keyword, status);

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setKeyword(keyword);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setMinPrice(minPrice);
        searchRequest.setMaxPrice(maxPrice);
        if (status != null) {
            searchRequest.setStatus(com.commercecoupon.enums.ProductStatus.valueOf(status));
        }
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setPage(page);
        searchRequest.setSize(size);

        ProductPageResponse response = productService.getAllProductsForAdmin(searchRequest);
        return ResponseEntity.ok(ApiResponse.success("관리자용 상품 목록 조회 성공", response));
    }

    /**
     * 상품 생성
     */
    @PostMapping
    @Operation(summary = "상품 생성", description = "새로운 상품을 등록합니다")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        log.info("상품 생성 요청: name={}", request.getName());

        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success("상품 생성 성공", response));
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        log.info("상품 수정 요청: productId={}", productId);

        ProductDetailResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success("상품 수정 성공", response));
    }

    /**
     * 상품 삭제 (상태 변경)
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "상품을 비활성화합니다")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {

        log.info("상품 삭제 요청: productId={}", productId);

        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다"));
    }

    /**
     * 재고 관리
     */
    @PatchMapping("/{productId}/stock")
    @Operation(summary = "재고 수정", description = "상품의 재고를 수정합니다")
    public ResponseEntity<ApiResponse<String>> updateStock(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "새로운 재고 수량", example = "100") @RequestParam Integer stock) {

        log.info("재고 수정 요청: productId={}, stock={}", productId, stock);

        productService.updateStock(productId, stock);
        return ResponseEntity.ok(ApiResponse.success("재고가 수정되었습니다"));
    }

    /**
     * 상품 상태 변경
     */
    @PatchMapping("/{productId}/status")
    @Operation(summary = "상품 상태 변경", description = "상품의 판매 상태를 변경합니다")
    public ResponseEntity<ApiResponse<String>> updateProductStatus(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "새로운 상태", example = "ACTIVE") @RequestParam String status) {

        log.info("상품 상태 변경 요청: productId={}, status={}", productId, status);

        productService.updateProductStatus(productId, com.commercecoupon.enums.ProductStatus.valueOf(status));
        return ResponseEntity.ok(ApiResponse.success("상품 상태가 변경되었습니다"));
    }

    /**
     * 재고 부족 상품 조회
     */
    @GetMapping("/low-stock")
    @Operation(summary = "재고 부족 상품 조회", description = "재고가 부족한 상품 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @Parameter(description = "재고 임계값", example = "10") @RequestParam(defaultValue = "10") Integer threshold) {

        log.info("재고 부족 상품 조회: threshold={}", threshold);

        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success("재고 부족 상품 조회 성공", products));
    }
}