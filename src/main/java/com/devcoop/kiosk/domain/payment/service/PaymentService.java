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
import com.devcoop.kiosk.domain.payment.dto.PaymentRequest.PaymentItem;
import com.devcoop.kiosk.domain.payment.dto.PaymentResponse;
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
import lombok.Getter;
import lombok.Builder;

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
    public PaymentResponse executeAllTransactions(PaymentRequest payment, String userCode) throws GlobalException {
        try {
            PaymentContext context = initializePaymentContext(payment, userCode);
            
            return switch (payment.type()) {
                case CHARGE -> handleChargePayment(payment, context);
                case PAYMENT -> handleNormalPayment(payment, context);
                case MIXED -> handleMixedPayment(payment, context);
                default -> throw new GlobalException(ErrorCode.INVALID_PAYMENT_REQUEST);
            };
            
        } catch (GlobalException e) {
            log.error("결제 처리 중 오류 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
    }

    @Getter
    @Builder
    private static class PaymentContext {
        private final String userCode;
        private final String userEmail;
        private final int initialPoint;
        private int currentPoint;
        private PgResponse cardResponse;
        
        public void updatePoint(int newPoint) {
            this.currentPoint = newPoint;
        }
    }

    private PaymentContext initializePaymentContext(PaymentRequest payment, String userCode) {
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
                
        return PaymentContext.builder()
                .userCode(userCode)
                .userEmail(user.getUserEmail())
                .initialPoint(user.getUserPoint())
                .currentPoint(user.getUserPoint())
                .build();
    }

    private PaymentResponse handleChargePayment(PaymentRequest payment, PaymentContext context) {
        int chargeAmount = payment.charge().amount();
        
        // 카드 결제
        context.cardResponse = processCardPayment(
            chargeAmount, 
            createChargeProduct(chargeAmount), 
            context.getUserEmail()
        );
        
        // 포인트 충전
        int updatedPoints = pointService.chargePoints(context.getUserCode(), chargeAmount);
        context.updatePoint(updatedPoints);
        
        // 로그 저장
        saveChargeLog(context, chargeAmount);
        
        return PaymentResponse.forCharge(
            chargeAmount,
            updatedPoints,
            context.getCardResponse().getTransaction().getApprovalNumber()
        );
    }

    private PaymentResponse handleNormalPayment(PaymentRequest payment, PaymentContext context) {
        int totalAmount = payment.payment().totalAmount();
        
        if (context.getCurrentPoint() >= totalAmount) {
            return handlePointOnlyPayment(payment, context, totalAmount);
        }
        return handlePointAndCardPayment(payment, context, totalAmount);
    }

    private PaymentResponse handlePointOnlyPayment(PaymentRequest payment, PaymentContext context, int totalAmount) {
        // 포인트 차감
        int updatedPoints = pointService.deductPoints(context.getUserCode(), totalAmount);
        context.updatePoint(updatedPoints);
        
        // 로그 저장
        savePaymentLog(context, totalAmount, 0);
        saveReceipt(payment, context.getUserCode());
        
        return PaymentResponse.forPayment(
            totalAmount,
            totalAmount,
            null,
            updatedPoints,
            null
        );
    }

    private PaymentResponse handlePointAndCardPayment(PaymentRequest payment, PaymentContext context, int totalAmount) {
        int pointsToUse = context.getCurrentPoint();
        int cardAmount = totalAmount - pointsToUse;
        
        // 카드 결제
        context.cardResponse = processCardPayment(
            cardAmount,
            createPaymentProducts(payment.payment().items(), pointsToUse),
            context.getUserEmail()
        );
        
        // 포인트 차감
        int updatedPoints = pointService.deductPoints(context.getUserCode(), pointsToUse);
        context.updatePoint(updatedPoints);
        
        // 로그 저장
        savePaymentLog(context, pointsToUse, cardAmount);
        saveReceipt(payment, context.getUserCode());
        
        return PaymentResponse.forPayment(
            totalAmount,
            pointsToUse,
            cardAmount,
            updatedPoints,
            context.getCardResponse().getTransaction().getApprovalNumber()
        );
    }

    private PaymentResponse handleMixedPayment(PaymentRequest payment, PaymentContext context) {
        int chargeAmount = payment.charge().amount();
        int paymentAmount = payment.payment().totalAmount();
        int totalAmount = chargeAmount + paymentAmount;
        
        // 카드 결제
        context.cardResponse = processCardPayment(
            totalAmount,
            createMixedPaymentProducts(payment, chargeAmount),
            context.getUserEmail()
        );
        
        // 포인트 충전
        int updatedPoints = pointService.chargePoints(context.getUserCode(), chargeAmount);
        context.updatePoint(updatedPoints);
        
        // 로그 저장
        saveChargeLog(context, chargeAmount);
        saveReceipt(payment, context.getUserCode());
        
        return PaymentResponse.forMixed(
            totalAmount,
            chargeAmount,
            paymentAmount,
            updatedPoints,
            context.getCardResponse().getTransaction().getApprovalNumber()
        );
    }

    // 헬퍼 메서드들
    private void savePaymentLog(PaymentContext context, int pointsUsed, int cardAmount) {
        String transactionId = context.getCardResponse() != null ? 
            context.getCardResponse().getTransaction().getTransactionId() : null;
            
        payLogService.savePayLog(
            context.getUserCode(),
            context.getInitialPoint(),
            pointsUsed,
            cardAmount,
            transactionId
        );
    }

    private void saveChargeLog(PaymentContext context, int chargeAmount) {
        chargeLogService.saveChargeLog(
            context.getUserCode(),
            context.getInitialPoint(),
            chargeAmount,
            context.getCardResponse().getTransaction().getTransactionId()
        );
    }

    private void saveReceipt(PaymentRequest payment, String userCode) {
        if (payment.payment() != null && payment.payment().items() != null) {
            receiptService.saveReceipt(payment.payment().items(), userCode);
        }
    }

    // 기존 헬퍼 메서드들은 그대로 유지
    private PgResponse processCardPayment(int amount, List<PaymentProduct> products, String userEmail) {
        PgResponse cardResponse = pgService.processCardPayment(amount, products, userEmail);
        if (!cardResponse.isSuccess()) {
            throw new GlobalException(ErrorCode.PAYMENT_FAILED);
        }
        return cardResponse;
    }

    private List<PaymentProduct> createChargeProduct(int chargeAmount) {
        return List.of(PaymentProduct.builder()
            .name("아리페이 충전")
            .price(chargeAmount)
            .quantity(1)
            .total(chargeAmount)
            .build());
    }

    private List<PaymentProduct> createMixedPaymentProducts(PaymentRequest payment, int chargeAmount) {
        List<PaymentProduct> products = new ArrayList<>();
        
        // 충전 금액을 첫 번째 상품으로 추가
        products.add(PaymentProduct.builder()
            .name("아리페이 충전")
            .price(chargeAmount)
            .quantity(1)
            .total(chargeAmount)
            .build());
        
        // 구매 상품들 추가
        products.addAll(payment.payment().items().stream()
            .map(item -> PaymentProduct.builder()
                .name(item.itemName())
                .price(item.itemPrice())
                .quantity(item.quantity())
                .total(item.totalPrice())
                .build())
            .collect(Collectors.toList()));
            
        return products;
    }

    private List<PaymentProduct> createPaymentProducts(List<PaymentItem> items, int pointsUsed) {
        List<PaymentProduct> products = new ArrayList<>();
        
        // 아리페이 사용 금액을 음수로 추가
        products.add(PaymentProduct.builder()
            .name("아리페이 사용")
            .price(-pointsUsed)
            .quantity(1)
            .total(-pointsUsed)
            .build());
        
        // 실제 상품 금액 추가
        products.addAll(items.stream()
            .map(item -> PaymentProduct.builder()
                .name(item.itemName())
                .price(item.itemPrice())
                .quantity(item.quantity())
                .total(item.totalPrice())
                .build())
            .collect(Collectors.toList()));
            
        return products;
    }
}
