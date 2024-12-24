package com.devcoop.kiosk.domain.item.presentation.dto;

import com.devcoop.kiosk.domain.item.types.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "상품 정보 응답")
public record ItemResponse(
        @Schema(description = "상품 ID", example = "1")
        Integer itemId,
        
        @Schema(description = "상품 코드", example = "ITEM001")
        String itemCode,
        
        @Schema(description = "상품명", example = "탐사 미니 물티슈")
        String itemName,
        
        @Schema(description = "상품 가격", example = "1500")
        int itemPrice,
        
        @Schema(description = "이벤트 상태", example = "ONE_PLUS_ONE")
        EventType eventStatus,

        @Schema(description = "상품 카테고리", example = "물티슈")
        String itemCategory
) {}
