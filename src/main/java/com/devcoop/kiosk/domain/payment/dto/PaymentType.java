package com.devcoop.kiosk.domain.payment.dto;

public enum PaymentType {
    CHARGE,     // 충전만
    PAYMENT,    // 결제만
    MIXED       // 충전 + 결제
} 