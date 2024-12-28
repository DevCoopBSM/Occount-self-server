package com.devcoop.kiosk.domain.pg;

import lombok.Builder;

@Builder
public record CardInfo(
    String acquirerCode,    // 카드 매입사 코드
    String acquirerName,    // 카드 매입사명
    String issuerCode,      // 카드 발급사 코드
    String issuerName,      // 카드 발급사명
    CardType cardType,      // 카드 종류 (CREDIT/DEBIT)
    String cardCategory,    // 카드 구분
    String cardName,        // 카드명
    String cardBrand       // 카드 브랜드
) {} 