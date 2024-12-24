package com.devcoop.kiosk.domain.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductInfo {
    private String name;
    private int price;
    private int quantity;
    private int total;
} 