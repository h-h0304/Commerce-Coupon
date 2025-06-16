package com.commercecoupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 정보 응답")
public class CategoryResponse {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리명", example = "노트북")
    private String name;

    @Schema(description = "카테고리 설명", example = "각종 노트북 컴퓨터")
    private String description;

    @Schema(description = "상품 수", example = "25")
    private Long productCount;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "활성 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}