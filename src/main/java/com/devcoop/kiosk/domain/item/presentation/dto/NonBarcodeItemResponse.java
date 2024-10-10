package com.devcoop.kiosk.domain.item.presentation.dto;

import lombok.Builder;

@Builder
public record NonBarcodeItemResponse(
        String itemName,
        int itemPrice
) {

}
