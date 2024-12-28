package com.devcoop.kiosk.domain.receipt.types;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SaleType {
    NORMAL("정상결제");

    private final String name;
}
