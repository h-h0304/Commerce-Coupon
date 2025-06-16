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
@Schema(description = "주문 페이지 응답")
public class OrderPageResponse {

    @Schema(description = "주문 목록")
    private List<OrderSummaryResponse> orders;

    @Schema(description = "현재 페이지", example = "0")
    private Integer currentPage;

    @Schema(description = "페이지 크기", example = "10")
    private Integer pageSize;

    @Schema(description = "총 요소 수", example = "25")
    private Long totalElements;

    @Schema(description = "총 페이지 수", example = "3")
    private Integer totalPages;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private Boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean isLast;
}