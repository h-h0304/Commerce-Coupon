package com.commercecoupon.dto.request;

import com.commercecoupon.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "상품 검색 요청")
public class ProductSearchRequest {

    @Schema(description = "검색 키워드", example = "맥북")
    private String keyword;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "최소 가격", example = "100000")
    private Integer minPrice;

    @Schema(description = "최대 가격", example = "5000000")
    private Integer maxPrice;

    @Schema(description = "상품 상태", example = "ACTIVE")
    private ProductStatus status = ProductStatus.ACTIVE;

    @Schema(description = "추천 상품만 조회", example = "false")
    private Boolean featuredOnly = false;

    @Schema(description = "정렬 기준", example = "createdAt",
            allowableValues = {"createdAt", "price", "salesCount", "viewCount"})
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";

    @Schema(description = "페이지 번호", example = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;
}