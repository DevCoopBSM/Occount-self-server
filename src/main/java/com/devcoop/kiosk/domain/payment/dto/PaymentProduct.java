package com.devcoop.kiosk.domain.payment.dto;

import lombok.Builder;

@Builder
public record PaymentProduct(
    String name,      // 상품명
    int price,        // 단가
    int quantity,     // 수량
    int total        // 총액
) {} 