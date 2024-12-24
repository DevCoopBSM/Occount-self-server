package com.devcoop.kiosk.domain.pg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionInfo {
    private String messageNumber;
    private String typeCode;
    private String cardNumber;
    private int amount;
    private int installmentMonths;
    private String cancelType;
    private String approvalNumber;
    private String approvalDate;
    private String approvalTime;
    private String transactionId;
    private String terminalId;
    private String merchantNumber;
    private String rejectCode;
    private String rejectMessage;
} 