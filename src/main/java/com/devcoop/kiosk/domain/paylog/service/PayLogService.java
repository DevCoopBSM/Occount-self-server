package com.devcoop.kiosk.domain.paylog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.paylog.PayLog;
import com.devcoop.kiosk.domain.paylog.repository.PayLogRepository;
import com.devcoop.kiosk.domain.paylog.types.EventType;
import com.devcoop.kiosk.domain.paylog.types.PaymentType;
import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayLogService {
    private final PayLogRepository payLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void savePayLog(String userEmail, int beforePoint, int pointsUsed, int cardAmount, String paymentId) throws GlobalException {
        log.info("결제 로그 저장 시작 - userEmail: {}, beforePoint: {}, pointsUsed: {}, cardAmount: {}, paymentId: {}", 
                userEmail, beforePoint, pointsUsed, cardAmount, paymentId);
                
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }
        
        PaymentType payType = determinePaymentType(pointsUsed, cardAmount);
        log.debug("결제 유형 결정: {}", payType);
        
        PayLog payLog = PayLog.builder()
                .userCode(user.getUserCode())
                .beforePoint(beforePoint)
                .payedPoint(pointsUsed)
                .afterPoint(beforePoint - pointsUsed)
                .managedEmail("Kiosk")
                .payType(payType)
                .eventType(EventType.NONE)
                .cardAmount(cardAmount)
                .paymentId(paymentId)
                .build();
                
        payLogRepository.save(payLog);
        log.info("결제 로그 저장 완료 - payType: {}, afterPoint: {}", payType, beforePoint - pointsUsed);
    }
    
    private PaymentType determinePaymentType(int pointsUsed, int cardAmount) {
        if (pointsUsed < 0 || cardAmount < 0) {
            log.warn("잘못된 결제 금액 - pointsUsed: {}, cardAmount: {}", pointsUsed, cardAmount);
        }
        
        if (cardAmount > 0 && pointsUsed > 0) {
            return PaymentType.MIXED;
        } else if (cardAmount > 0) {
            return PaymentType.CARD;
        } else if (pointsUsed > 0) {
            return PaymentType.POINT;
        } else {
            log.warn("결제 금액이 모두 0원입니다");
            return PaymentType.POINT; // 기본값
        }
    }
} 