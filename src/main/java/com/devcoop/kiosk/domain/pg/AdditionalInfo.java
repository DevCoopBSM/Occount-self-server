package com.devcoop.kiosk.domain.pg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalInfo {
    private String approvalStatus;      // 승인상태
    private String approvalCode;        // 승인코드
    private String icCreditApproval;    // IC신용승인
    private String transactionUuid;     // 거래 UUID
    private String vanMessage;          // VAN 통신 메시지
    private Float processingTime;       // 처리 소요 시간(초)
    private String requestAt;           // 요청 시각
    private String responseAt;          // 응답 시각
} 