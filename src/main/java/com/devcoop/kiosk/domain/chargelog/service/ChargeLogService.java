package com.devcoop.kiosk.domain.chargelog.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.chargelog.ChargeLog;
import com.devcoop.kiosk.domain.chargelog.presentation.ChargeLogRepository;
import com.devcoop.kiosk.global.exception.GlobalException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargeLogService {
    private final ChargeLogRepository chargeLogRepository;
    
    @Transactional
    public void saveChargeLog(String userCode, int beforePoint, int chargedPoint, String paymentId) throws GlobalException {
        log.info("충전 로그 저장 시작 - userCode: {}, beforePoint: {}, chargedPoint: {}, paymentId: {}", 
                userCode, beforePoint, chargedPoint, paymentId);
                
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
        log.info("충전 로그 저장 완료 - userCode: {}, afterPoint: {}", userCode, beforePoint + chargedPoint);
    }
}