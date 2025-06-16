package com.commercecoupon.dto.request;

import com.commercecoupon.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "상품 수정 요청")
public class ProductUpdateRequest {

    @Schema(description = "상품명", example = "MacBook Pro 14인치")
    @Size(max = 200, message = "상품명은 200자 이하여야 합니다")
    private String name;

    @Schema(description = "상품 설명", example = "최신 M3 칩셋을 탑재한 고성능 노트북")
    @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다")
    private String description;

    @Schema(description = "가격", example = "2490000")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private Integer price;

    @Schema(description = "재고 수량", example = "10")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다")
    private Integer stock;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "상세 이미지 URLs")
    private List<String> detailImageUrls;

    @Schema(description = "상품 상태", example = "ACTIVE")
    private ProductStatus status;

    @Schema(description = "추천 상품 여부", example = "false")
    private Boolean isFeatured;

    @Schema(description = "검색 태그", example = "노트북,맥북,애플")
    private String tags;
}