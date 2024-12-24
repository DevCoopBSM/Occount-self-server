package com.devcoop.kiosk.domain.payment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.chargelog.service.ChargeLogService;
import com.devcoop.kiosk.domain.paylog.service.PayLogService;
import com.devcoop.kiosk.domain.payment.dto.PaymentProduct;
import com.devcoop.kiosk.domain.payment.dto.PaymentRequest;
import com.devcoop.kiosk.domain.payment.dto.PaymentResponse;
import com.devcoop.kiosk.domain.payment.dto.PaymentRequest.PaymentItem;
import com.devcoop.kiosk.domain.pg.dto.PgResponse;
import com.devcoop.kiosk.domain.pg.service.PgService;
import com.devcoop.kiosk.domain.point.service.PointService;
import com.devcoop.kiosk.domain.receipt.service.ReceiptService;
import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PgService pgService;
    private final PointService pointService;
    private final PayLogService payLogService;
    private final ChargeLogService chargeLogService;
    private final ReceiptService receiptService;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse executeAllTransactions(PaymentRequest payment) throws GlobalException {
        try {
            String userId = payment.userInfo().id();
            User user = userRepository.findByUserCode(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
            
            int currentPoints = user.getUserPoint();
            
            // type에 따른 분기 처리
            switch (payment.type()) {
                case CHARGE:
                    return processChargeOnly(payment, currentPoints);
                case PAYMENT:
                    return processNormalPayment(payment, currentPoints);
                case MIXED:
                    return processMixedPayment(payment, currentPoints);
                default:
                    throw new GlobalException(ErrorCode.INVALID_PAYMENT_REQUEST);
            }
            
        } catch (GlobalException e) {
            log.error("결제 처리 중 오류 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private PaymentResponse processChargeOnly(PaymentRequest payment, int currentPoints) throws GlobalException {
        String userId = payment.userInfo().id();
        int chargeAmount = payment.charge().amount();
        
        PgResponse cardResponse = 
            pgService.processCardPayment(chargeAmount, List.of(
                PaymentProduct.builder()
                    .name("아리페이 충전")
                    .price(chargeAmount)
                    .quantity(1)
                    .total(chargeAmount)
                    .build()
            ));
            
        log.info("카드 결제 응답 - 성공 여부: {}, 메시지: {}, 거래 ID: {}", 
            cardResponse.isSuccess(), 
            cardResponse.getMessage(), 
            cardResponse.getTransaction().getTransactionId());
            
        if (!cardResponse.isSuccess()) {
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
        
        int updatedPoints = pointService.chargePoints(userId, chargeAmount);
        
        // 충전 로그 저장
        chargeLogService.saveChargeLog(
            userId,
            currentPoints,
            chargeAmount,
            cardResponse.getTransaction().getTransactionId()  // 거래 ID 저장
        );
        
        return PaymentResponse.forCharge(
            chargeAmount,
            updatedPoints,
            cardResponse.getTransaction().getApprovalNumber()
        );
    }

    private PaymentResponse processMixedPayment(PaymentRequest payment, int currentPoints) throws GlobalException {
        try {
            String userId = payment.userInfo().id();
            int chargeAmount = payment.charge().amount();
            int paymentAmount = payment.payment().totalAmount();
            
            // 결제 상품 목록 생성
            List<PaymentProduct> products = new ArrayList<>();
            
            // 충전 금액을 첫 번째 상품으로 추가
            products.add(PaymentProduct.builder()
                .name("아리페이 충전")
                .price(chargeAmount)
                .quantity(1)
                .total(chargeAmount)
                .build());
            
            // 구매 상품들 추가
            for (PaymentItem item : payment.payment().items()) {
                products.add(PaymentProduct.builder()
                    .name(item.itemName())
                    .price(item.itemPrice())
                    .quantity(item.quantity())
                    .total(item.totalPrice())
                    .build());
            }
            
            int totalAmount = paymentAmount + chargeAmount;
            
            log.info("혼합결제 요청 - 총금액: {}원 (충전: {}원, 결제: {}원), 상품개수: {}개", 
                totalAmount, chargeAmount, paymentAmount, products.size());
            
            // 카드 결제 처리
            PgResponse cardResponse = pgService.processCardPayment(totalAmount, products);
            
            log.info("카드 결제 응답 - 성공 여부: {}, 메시지: {}, 거래 ID: {}", 
                cardResponse.isSuccess(), 
                cardResponse.getMessage(), 
                cardResponse.getTransaction().getTransactionId());
            
            if (!cardResponse.isSuccess()) {
                log.error("카드 결제 실패 - {}", cardResponse.getMessage());
                return PaymentResponse.error(cardResponse.getMessage());
            }
                
            // 포인트 충전
            int updatedPoints = pointService.chargePoints(userId, chargeAmount);
            
            // 결제 로그 저장
            payLogService.savePayLog(
                userId,              
                currentPoints,       // beforePoint
                0,                  // pointsUsed (충전이므로 0)
                totalAmount,        // cardAmount
                cardResponse.getTransaction().getTransactionId()  // 거래 ID 저장
            );
            
            // 영수증 저장
            if (!payment.payment().items().isEmpty()) {
                receiptService.saveReceipt(payment.payment().items(), userId);
            }
            
            log.info("혼합결제 완료 - 승인번호: {}, 거래ID: {}, 잔여포인트: {}", 
                cardResponse.getTransaction().getApprovalNumber(),
                cardResponse.getTransaction().getTransactionId(),
                updatedPoints);
            
            return PaymentResponse.forMixed(
                totalAmount,
                chargeAmount,
                paymentAmount,
                updatedPoints,
                cardResponse.getTransaction().getApprovalNumber()
            );
            
        } catch (Exception e) {
            log.error("혼합결제 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private PaymentResponse processNormalPayment(PaymentRequest payment, int currentPoints) throws GlobalException {
        String userId = payment.userInfo().id();
        int totalAmount = payment.payment().totalAmount();
        
        if (currentPoints >= totalAmount) {
            // 포인트로만 결제
            int updatedPoints = pointService.deductPoints(userId, totalAmount);
            
            // 결제 로그 저장 (포인트만 사용)
            payLogService.savePayLog(
                userId, 
                currentPoints,
                totalAmount,  // pointsUsed
                0,           // cardAmount
                null        // 포인트만 사용시 승인번호 없음
            );
            
            // 영수증 저장
            receiptService.saveReceipt(payment.payment().items(), userId);
            
            return PaymentResponse.forPayment(
                totalAmount,
                totalAmount,
                null,
                updatedPoints,
                null
            );
            
        } else {
            // 포인트 + 카드 혼합 결제
            int cardAmount = totalAmount - currentPoints;
            int pointsUsed = currentPoints;
            
            log.info("결제 금액 상세 - 총액: {}원, 아리페이사용: {}원, 카드결제: {}원", 
                totalAmount, pointsUsed, cardAmount);
            
            List<PaymentProduct> products = new ArrayList<>();
            
            // 1. 아리페이 사용 금액을 음수로 추가
            products.add(PaymentProduct.builder()
                .name("아리페이 사용")
                .price(-pointsUsed)
                .quantity(1)
                .total(-pointsUsed)
                .build());
            
            // 2. 실제 상품 금액 추가
            products.addAll(payment.payment().items().stream()
                .map(item -> PaymentProduct.builder()
                    .name(item.itemName())
                    .price(item.itemPrice())
                    .quantity(item.quantity())
                    .total(item.totalPrice())
                    .build())
                .collect(Collectors.toList()));
            
            // 카드 결제 시도
            PgResponse cardResponse = 
                pgService.processCardPayment(cardAmount, products);
                
            log.info("카드 결제 응답 - 성공 여부: {}, 메시지: {}, 거래 ID: {}", 
                cardResponse.isSuccess(), 
                cardResponse.getMessage(), 
                cardResponse.getTransaction().getTransactionId());
            
            if (!cardResponse.isSuccess()) {
                throw new GlobalException(ErrorCode.PAYMENT_FAILED);
            }
            
            // 2. 카드 결제 성공 후 포인트 차감
            int updatedPoints = pointService.deductPoints(userId, pointsUsed);
            
            // 3. 결제 로그 저장
            payLogService.savePayLog(
                userId, 
                currentPoints,
                pointsUsed,
                cardAmount,
                cardResponse.getTransaction().getTransactionId()  // 거래 ID 저장
            );
            
            // 4. 영수증 저장
            receiptService.saveReceipt(payment.payment().items(), userId);
            
            return PaymentResponse.forPayment(
                totalAmount,
                pointsUsed,
                cardAmount,
                updatedPoints,
                cardResponse.getTransaction().getApprovalNumber()
            );
        }
    }
}
