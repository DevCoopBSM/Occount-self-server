package com.devcoop.kiosk.domain.payment.dto;

import com.devcoop.kiosk.domain.paylog.types.PaymentType;

import lombok.Builder;

@Builder
public record PaymentResponse(
    String status,             // success, error
    PaymentType type,         // enum으로 변경
    Integer totalAmount,      // 전체 거래 금액
    Integer chargedAmount,    // 충전 금액 (CHARGE, MIXED)
    Integer paymentAmount,    // 결제 금액 (PAYMENT, MIXED)
    Integer pointsUsed,       // 사용된 포인트 (PAYMENT)
    Integer cardAmount,       // 카드 결제 금액
    Integer remainingPoints,  // 최종 잔액
    String approvalNumber,    // 카드 승인번호
    String transactionId,     // 거래 ID 추가
    String message            // 오류 메시지 추가
) {
    public static PaymentResponse forCharge(int chargedAmount, int remainingPoints, String approvalNumber) {
        return PaymentResponse.builder()
            .status("success")
            .type(PaymentType.CARD)
            .chargedAmount(chargedAmount)
            .totalAmount(chargedAmount)
            .remainingPoints(remainingPoints)
            .approvalNumber(approvalNumber)
            .build();
    }

    public static PaymentResponse forPayment(
            int totalAmount, 
            int pointsUsed, 
            Integer cardAmount, 
            int remainingPoints, 
            String approvalNumber) {
        return PaymentResponse.builder()
            .status("success")
            .type(cardAmount != null ? PaymentType.MIXED : PaymentType.POINT)
            .totalAmount(totalAmount)
            .paymentAmount(totalAmount)
            .pointsUsed(pointsUsed)
            .cardAmount(cardAmount)
            .remainingPoints(remainingPoints)
            .approvalNumber(approvalNumber)
            .build();
    }

    public static PaymentResponse forMixed(
            int totalAmount, 
            int chargedAmount, 
            int paymentAmount, 
            int remainingPoints, 
            String approvalNumber) {
        return PaymentResponse.builder()
            .status("success")
            .type(PaymentType.MIXED)
            .totalAmount(totalAmount)
            .chargedAmount(chargedAmount)
            .paymentAmount(paymentAmount)
            .remainingPoints(remainingPoints)
            .approvalNumber(approvalNumber)
            .build();
    }

    public static PaymentResponse error(String message) {
        return PaymentResponse.builder()
            .status("error")
            .message(message)
            .build();
    }
} 