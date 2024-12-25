package com.devcoop.kiosk.global.utils.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentApiKeyGenerator {
    
    @Value("${payment.api.secret}")
    private String secretKey;
    
    private static final int TIME_WINDOW = 300; // 5분

    public String generateApiKey() {
        try {
            long timestamp = Instant.now().getEpochSecond();
            long timeCounter = timestamp / TIME_WINDOW;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            
            String message = String.valueOf(timeCounter);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            
            // 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.error("결제 API 키 생성 중 오류 발생", e);
            throw new RuntimeException("API 키 생성 실패", e);
        }
    }
}