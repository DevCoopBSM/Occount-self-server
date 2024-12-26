package com.devcoop.kiosk.domain.pg.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.devcoop.kiosk.domain.payment.ProductInfo;
import com.devcoop.kiosk.domain.payment.dto.PaymentProduct;
import com.devcoop.kiosk.domain.pg.dto.PgRequest;
import com.devcoop.kiosk.domain.pg.dto.PgResponse;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PgService {
    private final WebClient webClient;
    private final CardPaymentLogService cardPaymentLogService;
    
    @Transactional
    public PgResponse processCardPayment(int amount, List<PaymentProduct> products) {
        PgRequest request = createPaymentRequest(amount, products);
        PgResponse response = executePayment(request);
        savePaymentLog(response);
        return response;
    }
    
    private PgRequest createPaymentRequest(int amount, List<PaymentProduct> products) {
        List<ProductInfo> productInfos = products.stream()
            .map(p -> ProductInfo.builder()
                .name(p.getName())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .total(p.getTotal())
                .build())
            .collect(Collectors.toList());

        PgRequest request = PgRequest.builder()
            .amount(amount)
            .products(productInfos)
            .build();
            
        log.info("카드결제 요청 - 금액: {}원, 상품 수: {}개", amount, products.size());
        log.debug("카드결제 요청 데이터: {}", request);
        
        return request;
    }
    
    private PgResponse executePayment(PgRequest request) {
        try {
            return webClient.post()
                .uri("/api/payment")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value(), 
                    clientResponse -> Mono.error(new GlobalException(ErrorCode.INVALID_PAYMENT_REQUEST)))
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                    clientResponse -> {
                        log.warn("진행 중인 거래 감지됨");
                        return Mono.error(new GlobalException(ErrorCode.TRANSACTION_IN_PROGRESS));
                    })
                .onStatus(status -> status.value() == HttpStatus.REQUEST_TIMEOUT.value(),
                    clientResponse -> {
                        log.error("결제 요청 타임아웃 발생");
                        return Mono.error(new GlobalException(ErrorCode.PAYMENT_TIMEOUT));
                    })
                .bodyToMono(PgResponse.class)
                .doOnNext(this::logPaymentResponse)
                .timeout(Duration.ofSeconds(40))
                .doOnError(this::handlePaymentError)
                .block();
                
        } catch (Exception e) {
            if (e instanceof GlobalException) {
                throw e;
            }
            log.error("카드결제 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
    }
    
    private void logPaymentResponse(PgResponse response) {
        log.info("카드 결제 응답 - 성공 여부: {}, 메시지: {}, 거래 ID: {}, 승인번호: {}", 
            response.isSuccess(), 
            response.getMessage(), 
            response.getTransaction() != null ? response.getTransaction().getTransactionId() : "N/A",
            response.getTransaction() != null ? response.getTransaction().getApprovalNumber() : "N/A");
    }
    
    private void handlePaymentError(Throwable error) {
        if (error instanceof TimeoutException) {
            log.error("결제 요청 타임아웃 발생");
            throw new GlobalException(ErrorCode.PAYMENT_TIMEOUT);
        }
    }
    
    private void savePaymentLog(PgResponse response) {
        if (response != null && response.isSuccess()) {
            try {
                cardPaymentLogService.saveCardPaymentLog(response);
            } catch (Exception e) {
                log.error("카드결제 로그 저장 실패: {}", e.getMessage(), e);
                // 로그 저장 실패는 결제 실패로 처리하지 않음
            }
        }
    }
} 