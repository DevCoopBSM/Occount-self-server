package com.devcoop.kiosk.domain.pg.dto;

import com.devcoop.kiosk.domain.pg.AdditionalInfo;
import com.devcoop.kiosk.domain.pg.CardInfo;
import com.devcoop.kiosk.domain.pg.TransactionInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PgResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private TransactionInfo transaction;
    private CardInfo card;
    private AdditionalInfo additional;
    private String rawResponse;
} 