package com.devcoop.kiosk.domain.pg;

import lombok.Builder;

@Builder
public record TransactionInfo(
    String messageNumber,
    String typeCode,
    String cardNumber,
    int amount,
    int installmentMonths,
    String cancelType,
    String approvalNumber,
    String approvalDate,
    String approvalTime,
    String transactionId,
    String terminalId,
    String merchantNumber,
    String rejectCode,
    String rejectMessage
) {} 