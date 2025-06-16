package com.commercecoupon.controller;

import com.commercecoupon.dto.request.ProductSearchRequest;
import com.commercecoupon.dto.response.ApiResponse;
import com.commercecoupon.dto.response.ProductDetailResponse;
import com.commercecoupon.dto.response.ProductPageResponse;
import com.commercecoupon.dto.response.ProductResponse;
import com.commercecoupon.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품 API", description = "상품 조회 관련 기능")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (검색, 필터링, 페이징 지원)
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "검색 조건에 따른 상품 목록을 페이징으로 조회합니다")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "최소 가격") @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "추천 상품만 조회") @RequestParam(defaultValue = "false") Boolean featuredOnly,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size) {

        log.info("상품 목록 조회 요청: keyword={}, categoryId={}, page={}", keyword, categoryId, page);

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setKeyword(keyword);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setMinPrice(minPrice);
        searchRequest.setMaxPrice(maxPrice);
        searchRequest.setFeaturedOnly(featuredOnly);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setPage(page);
        searchRequest.setSize(size);

        ProductPageResponse response = productService.getProducts(searchRequest);
        return ResponseEntity.ok(ApiResponse.success("상품 목록 조회 성공", response));
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다 (조회수 증가)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {

        log.info("상품 상세 조회 요청: productId={}", productId);

        ProductDetailResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 상세 조회 성공", response));
    }

    /**
     * 추천 상품 목록 조회
     */
    @GetMapping("/featured")
    @Operation(summary = "추천 상품 조회", description = "관리자가 설정한 추천 상품 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        log.info("추천 상품 조회 요청");

        List<ProductResponse> response = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success("추천 상품 조회 성공", response));
    }

    /**
     * 인기 상품 목록 조회
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 상품 조회", description = "판매량 기준 인기 상품 Top 10을 조회합니다")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getPopularProducts() {
        log.info("인기 상품 조회 요청");

        List<ProductResponse> response = productService.getPopularProducts();
        return ResponseEntity.ok(ApiResponse.success("인기 상품 조회 성공", response));
    }

    /**
     * 최신 상품 목록 조회
     */
    @GetMapping("/latest")
    @Operation(summary = "최신 상품 조회", description = "최근 등록된 상품 Top 10을 조회합니다")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLatestProducts() {
        log.info("최신 상품 조회 요청");

        List<ProductResponse> response = productService.getLatestProducts();
        return ResponseEntity.ok(ApiResponse.success("최신 상품 조회 성공", response));
    }
}