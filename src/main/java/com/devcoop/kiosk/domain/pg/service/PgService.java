package com.devcoop.kiosk.domain.pg.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
@Slf4j
@RequiredArgsConstructor
public class PgService {
    private final WebClient webClient;
    
    public PgResponse processCardPayment(int amount, List<PaymentProduct> products) {
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
        log.info("카드결제 요청 데이터: {}", request);
            
        return webClient.post()
            .uri("/api/payment")
            .bodyValue(request)
            .retrieve()
            .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value(), 
                response -> Mono.error(new GlobalException(ErrorCode.INVALID_PAYMENT_REQUEST)))
            .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new GlobalException(ErrorCode.TRANSACTION_IN_PROGRESS)))
            .onStatus(status -> status.value() == HttpStatus.REQUEST_TIMEOUT.value(),
                response -> {
                    log.error("결제 요청 타임아웃 발생");
                    return Mono.error(new GlobalException(ErrorCode.PAYMENT_TIMEOUT));
                })
            .bodyToMono(PgResponse.class)
            .doOnNext(response -> log.info("카드 결제 응답 - 성공 여부: {}, 메시지: {}, 거래 ID: {}, 승인번호: {}", 
                response.isSuccess(), 
                response.getMessage(), 
                response.getTransaction() != null ? response.getTransaction().getTransactionId() : "N/A",
                response.getTransaction() != null ? response.getTransaction().getApprovalNumber() : "N/A"))
            .timeout(Duration.ofSeconds(40))
            .doOnError(error -> {
                if (error instanceof TimeoutException) {
                    log.error("결제 요청 타임아웃 발생");
                    throw new GlobalException(ErrorCode.PAYMENT_TIMEOUT);
                }
            })
            .block();
    }
} 