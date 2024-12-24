package com.devcoop.kiosk.domain.pg.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.devcoop.kiosk.domain.pg.dto.PgResponse;
import com.devcoop.kiosk.domain.pg.entity.CardPaymentLog;
import com.devcoop.kiosk.domain.pg.presentation.CardPaymentLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardPaymentLogService {
    private final CardPaymentLogRepository cardPaymentLogRepository;

    public void saveCardPaymentLog(PgResponse cardResponse) {
        if (cardResponse != null && cardResponse.isSuccess() && cardResponse.getTransaction() != null) {
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
                
            // 카드 정보가 있는 경우에만 추가
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
                
            CardPaymentLog paymentLog = builder.build();
            cardPaymentLogRepository.save(paymentLog);
            log.info("카드결제 성공 로그 저장 완료 - 거래ID: {}", paymentLog.getTransactionId());
        }
    }
} 