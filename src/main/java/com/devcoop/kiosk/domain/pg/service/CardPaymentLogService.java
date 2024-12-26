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
    public void saveCardPaymentLog(PgResponse cardResponse) {
        try {
            if (!isValidPaymentResponse(cardResponse)) {
                log.warn("유효하지 않은 결제 응답: {}", cardResponse);
                return;
            }

            CardPaymentLog paymentLog = buildCardPaymentLog(cardResponse);
            cardPaymentLogRepository.save(paymentLog);
            
            log.info("카드결제 로그 저장 완료 - 거래ID: {}, 금액: {}, 승인번호: {}", 
                paymentLog.getTransactionId(),
                paymentLog.getAmount(),
                paymentLog.getApprovalNumber());
                
        } catch (Exception e) {
            log.error("카드결제 로그 저장 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorCode.PAYMENT_LOG_SAVE_FAILED);
        }
    }

    private boolean isValidPaymentResponse(PgResponse cardResponse) {
        return cardResponse != null && 
               cardResponse.isSuccess() && 
               cardResponse.getTransaction() != null;
    }

    private CardPaymentLog buildCardPaymentLog(PgResponse cardResponse) {
        CardPaymentLog.CardPaymentLogBuilder builder = CardPaymentLog.builder()
            .transactionId(cardResponse.getTransaction().getTransactionId())
            .approvalNumber(cardResponse.getTransaction().getApprovalNumber())
            .cardNumber(cardResponse.getTransaction().getCardNumber())
            .amount(cardResponse.getTransaction().getAmount())
            .installmentMonths(cardResponse.getTransaction().getInstallmentMonths())
            .approvalDate(cardResponse.getTransaction().getApprovalDate())
            .approvalTime(cardResponse.getTransaction().getApprovalTime())
            .terminalId(cardResponse.getTransaction().getTerminalId())
            .merchantNumber(cardResponse.getTransaction().getMerchantNumber())
            .createdAt(LocalDateTime.now())
            .status("APPROVED");

        if (cardResponse.getCard() != null) {
            builder
                .issuerCode(cardResponse.getCard().getIssuerCode())
                .issuerName(cardResponse.getCard().getIssuerName())
                .cardType(cardResponse.getCard().getCardType() != null ? 
                    cardResponse.getCard().getCardType().name() : null)
                .cardCategory(cardResponse.getCard().getCardCategory())
                .cardName(cardResponse.getCard().getCardName())
                .cardBrand(cardResponse.getCard().getCardBrand());
        }

        return builder.build();
    }
} 