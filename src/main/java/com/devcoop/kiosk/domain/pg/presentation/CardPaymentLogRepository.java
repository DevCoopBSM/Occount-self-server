package com.devcoop.kiosk.domain.pg.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devcoop.kiosk.domain.pg.entity.CardPaymentLog;

@Repository
public interface CardPaymentLogRepository extends JpaRepository<CardPaymentLog, Long> {
    
    // 거래ID로 결제 내역 조회
    Optional<CardPaymentLog> findByTransactionId(String transactionId);
    
    // 승인번호로 결제 내역 조회
    Optional<CardPaymentLog> findByApprovalNumber(String approvalNumber);
    
    // 기간별 결제 내역 조회
    List<CardPaymentLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // 상태별 결제 내역 조회
    List<CardPaymentLog> findByStatus(String status);
} 