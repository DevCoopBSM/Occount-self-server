package com.devcoop.kiosk.domain.receipt.types;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SaleType {
    NORMAL("정상결제"), // '정상결제'로 수정 (NOMAL -> NORMAL)
    REFUND("환불결제");

    private final String name;
}
