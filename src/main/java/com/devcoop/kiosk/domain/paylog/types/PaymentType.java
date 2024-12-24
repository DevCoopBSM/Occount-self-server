package com.devcoop.kiosk.domain.paylog.types;

public enum PaymentType {
    POINT("포인트"),
    CARD("카드"),
    MIXED("포인트+카드");
    
    private final String description;
    
    PaymentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 