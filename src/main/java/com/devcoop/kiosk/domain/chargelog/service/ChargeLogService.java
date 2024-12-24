package com.devcoop.kiosk.domain.chargelog.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.devcoop.kiosk.domain.chargelog.ChargeLog;
import com.devcoop.kiosk.domain.chargelog.presentation.ChargeLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargeLogService {
    private final ChargeLogRepository chargeLogRepository;
    
    public void saveChargeLog(String userCode, int beforePoint, int chargedPoint, String paymentId) {
        ChargeLog chargeLog = ChargeLog.builder()
            .userCode(userCode)
            .chargeDate(LocalDateTime.now())
            .chargeType("4")
            .beforePoint(beforePoint)
            .chargedPoint(chargedPoint)
            .afterPoint(beforePoint + chargedPoint)
            .managedEmail("Kiosk")
            .reason("키오스크 충전")
            .paymentId(paymentId)
            .refundState("NONE")
            .build();
            
        chargeLogRepository.save(chargeLog);
    }
}