package com.devcoop.kiosk.domain.pg;

import lombok.Builder;

@Builder
public record AdditionalInfo(
    String approvalStatus,      // 승인상태
    String approvalCode,        // 승인코드
    String icCreditApproval,    // IC신용승인
    String transactionUuid,     // 거래 UUID
    String vanMessage,          // VAN 통신 메시지
    Float processingTime,       // 처리 소요 시간(초)
    String requestAt,           // 요청 시각
    String responseAt           // 응답 시각
) {} 