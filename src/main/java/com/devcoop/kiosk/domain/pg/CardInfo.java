package com.devcoop.kiosk.domain.pg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardInfo {
    private String acquirerCode;    // 카드 매입사 코드
    private String acquirerName;    // 카드 매입사명
    private String issuerCode;      // 카드 발급사 코드
    private String issuerName;      // 카드 발급사명
    private CardType cardType;      // 카드 종류 (CREDIT/DEBIT)
    private String cardCategory;    // 카드 구분
    private String cardName;        // 카드명
    private String cardBrand;       // 카드 브랜드
} 