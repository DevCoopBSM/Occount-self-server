package com.devcoop.kiosk.domain.pg.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

import com.devcoop.kiosk.domain.payment.ProductInfo;

@Getter
@Builder
public class PgRequest {
    private int amount;
    private List<ProductInfo> products;
} 