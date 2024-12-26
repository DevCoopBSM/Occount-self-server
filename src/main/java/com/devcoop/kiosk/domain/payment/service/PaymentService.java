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
            User user = findAndValidateUser(payment.userInfo().id());
            int currentPoints = user.getUserPoint();
            
            return switch (payment.type()) {
                case CHARGE -> processChargeOnly(payment, currentPoints);
                case PAYMENT -> processNormalPayment(payment, currentPoints);
                case MIXED -> processMixedPayment(payment, currentPoints);
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

    private User findAndValidateUser(String userId) {
        return userRepository.findByUserCode(userId)
            .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    private PaymentResponse processChargeOnly(PaymentRequest payment, int currentPoints) {
        String userId = payment.userInfo().id();
        int chargeAmount = payment.charge().amount();
        
        // 카드 결제 처리
        PgResponse cardResponse = processCardPayment(chargeAmount, createChargeProduct(chargeAmount));
        
        // 포인트 충전
        int updatedPoints = pointService.chargePoints(userId, chargeAmount);
        
        // 충전 로그 저장
        chargeLogService.saveChargeLog(
            userId,
            currentPoints,
            chargeAmount,
            cardResponse.getTransaction().getTransactionId()
        );
        
        return PaymentResponse.forCharge(
            chargeAmount,
            updatedPoints,
            cardResponse.getTransaction().getApprovalNumber()
        );
    }

    private PaymentResponse processMixedPayment(PaymentRequest payment, int currentPoints) {
        String userId = payment.userInfo().id();
        int chargeAmount = payment.charge().amount();
        int paymentAmount = payment.payment().totalAmount();
        int totalAmount = paymentAmount + chargeAmount;
        
        // 결제 상품 목록 생성
        List<PaymentProduct> products = createMixedPaymentProducts(payment, chargeAmount);
        
        log.info("혼합결제 요청 - 총금액: {}원 (충전: {}원, 결제: {}원), 상품개수: {}개", 
            totalAmount, chargeAmount, paymentAmount, products.size());
        
        // 카드 결제 처리
        PgResponse cardResponse = processCardPayment(totalAmount, products);
        
        // 포인트 충전
        int updatedPoints = pointService.chargePoints(userId, chargeAmount);
        
        // 결제 로그 저장
        payLogService.savePayLog(
            userId,              
            currentPoints,       
            0,                  // pointsUsed (충전이므로 0)
            totalAmount,        // cardAmount
            cardResponse.getTransaction().getTransactionId()
        );
        
        // 영수증 저장
        if (!payment.payment().items().isEmpty()) {
            receiptService.saveReceipt(payment.payment().items(), userId);
        }
        
        return PaymentResponse.forMixed(
            totalAmount,
            chargeAmount,
            paymentAmount,
            updatedPoints,
            cardResponse.getTransaction().getApprovalNumber()
        );
    }

    private PaymentResponse processNormalPayment(PaymentRequest payment, int currentPoints) {
        String userId = payment.userInfo().id();
        int totalAmount = payment.payment().totalAmount();
        
        if (currentPoints >= totalAmount) {
            return processPointOnlyPayment(payment, userId, currentPoints, totalAmount);
        } else {
            return processPointAndCardPayment(payment, userId, currentPoints, totalAmount);
        }
    }

    private PaymentResponse processPointOnlyPayment(
        PaymentRequest payment, String userId, int currentPoints, int totalAmount) {
        
        // 포인트 차감
        int updatedPoints = pointService.deductPoints(userId, totalAmount);
        
        // 결제 로그 저장
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
    }

    private PaymentResponse processPointAndCardPayment(
        PaymentRequest payment, String userId, int currentPoints, int totalAmount) {
        
        int cardAmount = totalAmount - currentPoints;
        int pointsUsed = currentPoints;
        
        log.info("결제 금액 상세 - 총액: {}원, 아리페이사용: {}원, 카드결제: {}원", 
            totalAmount, pointsUsed, cardAmount);
        
        // 카드 결제 처리
        PgResponse cardResponse = processCardPayment(cardAmount, 
            createPaymentProducts(payment.payment().items(), pointsUsed));
        
        // 포인트 차감
        int updatedPoints = pointService.deductPoints(userId, pointsUsed);
        
        // 결제 로그 저장
        payLogService.savePayLog(
            userId, 
            currentPoints,
            pointsUsed,
            cardAmount,
            cardResponse.getTransaction().getTransactionId()
        );
        
        // 영수증 저장
        receiptService.saveReceipt(payment.payment().items(), userId);
        
        return PaymentResponse.forPayment(
            totalAmount,
            pointsUsed,
            cardAmount,
            updatedPoints,
            cardResponse.getTransaction().getApprovalNumber()
        );
    }

    private PgResponse processCardPayment(int amount, List<PaymentProduct> products) {
        PgResponse cardResponse = pgService.processCardPayment(amount, products);
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
