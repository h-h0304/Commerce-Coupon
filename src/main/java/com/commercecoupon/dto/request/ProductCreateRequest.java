package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "상품 생성 요청")
public class ProductCreateRequest {

    @Schema(description = "상품명", example = "MacBook Pro 14인치", required = true)
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 200, message = "상품명은 200자 이하여야 합니다")
    private String name;

    @Schema(description = "상품 설명", example = "최신 M3 칩셋을 탑재한 고성능 노트북")
    @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다")
    private String description;

    @Schema(description = "가격", example = "2490000", required = true)
    @NotNull(message = "가격은 필수입니다")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private Integer price;

    @Schema(description = "재고 수량", example = "10")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다")
    private Integer stock = 0;

    @Schema(description = "카테고리 ID", example = "1", required = true)
    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;

    @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "상세 이미지 URLs")
    private List<String> detailImageUrls;

    @Schema(description = "추천 상품 여부", example = "false")
    private Boolean isFeatured = false;

    @Schema(description = "검색 태그", example = "노트북,맥북,애플")
    private String tags;
}