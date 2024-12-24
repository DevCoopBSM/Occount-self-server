package com.devcoop.kiosk.domain.paylog.presentation.dto;

import com.devcoop.kiosk.domain.paylog.PayLog;
import com.devcoop.kiosk.domain.paylog.types.EventType;
import com.devcoop.kiosk.domain.paylog.types.PaymentType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PayLogRequest(
        @NotBlank(message = "바코드는 필수 입력사항 입니다")
        String userCode, 
        int payedPoint // 결제된 포인트
) {

    public PayLog toEntity(int beforePoint, int afterPoint) { 
        return PayLog.builder()
                .userCode(userCode)
                .beforePoint(beforePoint)
                .payedPoint(payedPoint)
                .afterPoint(afterPoint) // 결제 후 남은 포인트
                .managedEmail("Kiosk")
                .payType(PaymentType.POINT) 
                .eventType(EventType.NONE)
                .build();
    }
}
