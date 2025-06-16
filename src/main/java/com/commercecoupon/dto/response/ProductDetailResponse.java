package com.commercecoupon.dto.response;

import com.commercecoupon.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 상세 정보 응답")
public class ProductDetailResponse {

    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품명", example = "MacBook Pro 14인치")
    private String name;

    @Schema(description = "상품 설명")
    private String description;

    @Schema(description = "가격", example = "2490000")
    private Integer price;

    @Schema(description = "재고 수량", example = "10")
    private Integer stock;

    @Schema(description = "대표 이미지 URL")
    private String imageUrl;

    @Schema(description = "상세 이미지 URLs")
    private List<String> detailImageUrls;

    @Schema(description = "카테고리 정보")
    private CategoryResponse category;

    @Schema(description = "상품 상태")
    private ProductStatus status;

    @Schema(description = "조회수", example = "150")
    private Integer viewCount;

    @Schema(description = "판매수", example = "25")
    private Integer salesCount;

    @Schema(description = "추천 상품 여부", example = "false")
    private Boolean isFeatured;

    @Schema(description = "검색 태그")
    private List<String> tags;

    @Schema(description = "관련 상품 목록")
    private List<ProductResponse> relatedProducts;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}