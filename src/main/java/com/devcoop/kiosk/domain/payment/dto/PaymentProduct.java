package com.devcoop.kiosk.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentProduct {
    private String name;      // 상품명
    private int price;        // 단가
    private int quantity;     // 수량
    private int total;        // 총액
} 