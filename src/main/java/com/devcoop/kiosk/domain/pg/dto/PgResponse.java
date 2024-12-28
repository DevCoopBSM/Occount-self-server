package com.devcoop.kiosk.domain.pg.dto;

import com.devcoop.kiosk.domain.pg.AdditionalInfo;
import com.devcoop.kiosk.domain.pg.CardInfo;
import com.devcoop.kiosk.domain.pg.TransactionInfo;

import lombok.Builder;

@Builder
public record PgResponse(
    boolean success,
    String message,
    String errorCode,
    TransactionInfo transaction,
    CardInfo card,
    AdditionalInfo additional,
    String rawResponse
) {} 