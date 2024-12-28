package com.devcoop.kiosk.domain.pg.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.pg.dto.PgResponse;
import com.devcoop.kiosk.domain.pg.entity.CardPaymentLog;
import com.devcoop.kiosk.domain.pg.presentation.CardPaymentLogRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardPaymentLogService {
    private final CardPaymentLogRepository cardPaymentLogRepository;

    @Transactional
    public void saveCardPaymentLog(PgResponse cardResponse, String userEmail) {
        try {
            if (!isValidPaymentResponse(cardResponse)) {
                log.warn("유효하지 않은 결제 응답: {}", cardResponse);
                return;
            }

            CardPaymentLog paymentLog = buildCardPaymentLog(cardResponse, userEmail);
            cardPaymentLogRepository.save(paymentLog);
            
            log.info("카드결제 로그 저장 완료 - 거래ID: {}, 사용자: {}, 금액: {}, 승인번호: {}", 
                paymentLog.getTransactionId(),
                paymentLog.getUserEmail(),
                paymentLog.getAmount(),
                paymentLog.getApprovalNumber());
                
        } catch (Exception e) {
            log.error("카드결제 로그 저장 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorCode.PAYMENT_LOG_SAVE_FAILED);
        }
    }

    private boolean isValidPaymentResponse(PgResponse cardResponse) {
        return cardResponse != null && 
               cardResponse.success() && 
               cardResponse.transaction() != null;
    }

    private CardPaymentLog buildCardPaymentLog(PgResponse cardResponse, String userEmail) {
        CardPaymentLog.CardPaymentLogBuilder builder = CardPaymentLog.builder()
            .userEmail(userEmail)
            .transactionId(cardResponse.transaction().transactionId())
            .approvalNumber(cardResponse.transaction().approvalNumber())
            .cardNumber(cardResponse.transaction().cardNumber())
            .amount(cardResponse.transaction().amount())
            .installmentMonths(cardResponse.transaction().installmentMonths())
            .approvalDate(cardResponse.transaction().approvalDate())
            .approvalTime(cardResponse.transaction().approvalTime())
            .terminalId(cardResponse.transaction().terminalId())
            .merchantNumber(cardResponse.transaction().merchantNumber())
            .createdAt(LocalDateTime.now())
            .status("APPROVED");

        if (cardResponse.card() != null) {
            builder
                .issuerCode(cardResponse.card().issuerCode())
                .issuerName(cardResponse.card().issuerName())
                .cardType(cardResponse.card().cardType() != null ? 
                    cardResponse.card().cardType().name() : null)
                .cardCategory(cardResponse.card().cardCategory())
                .cardName(cardResponse.card().cardName())
                .cardBrand(cardResponse.card().cardBrand());
        }

        return builder.build();
    }
} 