package com.devcoop.kiosk.domain.payment.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record PaymentRequest(
    PaymentType type,           // CHARGE, PAYMENT, MIXED
    UserInfo userInfo,
    ChargeRequest charge,       // 충전 요청 정보 (CHARGE, MIXED)
    PaymentDetails payment      // 결제 요청 정보 (PAYMENT, MIXED)
) {
    public record UserInfo(
        String id
    ) {}

    public record ChargeRequest(
        int amount,
        String method          // "CARD"
    ) {}

    public record PaymentDetails(
        List<PaymentItem> items,
        int totalAmount
    ) {}

    public record PaymentItem(
        String itemId,
        String itemName,
        int itemPrice,
        int quantity,
        int totalPrice
    ) {}
}


