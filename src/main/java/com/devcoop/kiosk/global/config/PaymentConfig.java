package com.devcoop.kiosk.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "payment.api")
@Getter
@Setter
public class PaymentConfig {
    private String url;
    private String apiKey;
} 