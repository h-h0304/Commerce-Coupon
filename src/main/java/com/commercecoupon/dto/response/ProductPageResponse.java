package com.commercecoupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 페이지 응답")
public class ProductPageResponse {

    @Schema(description = "상품 목록")
    private List<ProductResponse> products;

    @Schema(description = "현재 페이지", example = "0")
    private Integer currentPage;

    @Schema(description = "페이지 크기", example = "20")
    private Integer pageSize;

    @Schema(description = "총 요소 수", example = "150")
    private Long totalElements;

    @Schema(description = "총 페이지 수", example = "8")
    private Integer totalPages;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private Boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean isLast;
}