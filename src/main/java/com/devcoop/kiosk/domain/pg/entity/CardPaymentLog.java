package com.devcoop.kiosk.domain.pg.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "occount_cardPaymentLog")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CardPaymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;        // 거래고유번호
    private String approvalNumber;       // 승인번호
    private String cardNumber;           // 마스킹된 카드번호
    private Integer amount;              // 거래금액
    private Integer installmentMonths;   // 할부개월
    private String approvalDate;         // 승인일자 YYYYMMDD
    private String approvalTime;         // 승인시간 HHMMSS
    private String terminalId;           // 단말기번호
    private String merchantNumber;       // 가맹점번호

    // 카드 정보
    private String issuerCode;          // 카드 발급사 코드
    private String issuerName;          // 카드 발급사명
    private String cardType;            // 카드 종류 (CREDIT/DEBIT)
    private String cardCategory;        // 카드 구분
    private String cardName;            // 카드명
    private String cardBrand;           // 카드 브랜드(VISA/MASTER 등)

    private LocalDateTime createdAt;    // 생성일시
    private String status;              // 상태 (APPROVED, CANCELLED 등)
}