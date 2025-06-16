package com.commercecoupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "카테고리 생성 요청")
public class CategoryCreateRequest {

    @Schema(description = "카테고리명", example = "노트북", required = true)
    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(max = 100, message = "카테고리명은 100자 이하여야 합니다")
    private String name;

    @Schema(description = "카테고리 설명", example = "각종 노트북 컴퓨터")
    @Size(max = 500, message = "카테고리 설명은 500자 이하여야 합니다")
    private String description;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder = 0;
}