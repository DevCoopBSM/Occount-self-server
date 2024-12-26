package com.devcoop.kiosk.global.utils.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    @Value("${api.secret}")
    private String secretKey;
    
    private static final int TIME_WINDOW = 300; // 5분

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestApiKey = request.getHeader("X-API-Key");
        log.info("===== API 키 검증 시작 =====");
        log.info("수신된 API 키: {}", requestApiKey);
        log.info("사용된 비밀키: {}", secretKey);
        
        if (requestApiKey == null) {
            log.warn("API 키가 누락되었습니다");
            sendErrorResponse(response, "API 키가 필요합니다.");
            return;
        }

        if (!isValidApiKey(requestApiKey)) {
            log.warn("유효하지 않은 API 키: {}", requestApiKey);
            sendErrorResponse(response, "유효하지 않은 API 키입니다.");
            return;
        }
        
        log.info("API 키 검증 성공");
        filterChain.doFilter(request, response);
    }

    private boolean isValidApiKey(String requestApiKey) {
        try {
            long currentTime = Instant.now().getEpochSecond();
            log.info("현재 UTC 시간: {}", Instant.now());
            log.info("타임스탬프 (초): {}", currentTime);
            log.info("사용된 비밀키: {}", secretKey);
            
            for (int offset = -1; offset <= 1; offset++) {
                long timestamp = currentTime + (offset * TIME_WINDOW);
                long timeCounter = timestamp / TIME_WINDOW;
                
                log.info("검증 시도 [{}] - 타임스탬프: {}, 카운터: {}, 비밀키: {}", 
                    offset, timestamp, timeCounter, secretKey);
                
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
                );
                mac.init(secretKeySpec);
                
                String message = String.valueOf(timeCounter);
                byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
                
                StringBuilder expectedKey = new StringBuilder();
                for (byte b : hmacBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) expectedKey.append('0');
                    expectedKey.append(hex);
                }
                
                log.info("기대되는 키 [{}]: {} (비밀키: {})", offset, expectedKey, secretKey);
                
                if (MessageDigest.isEqual(
                    requestApiKey.getBytes(StandardCharsets.UTF_8),
                    expectedKey.toString().getBytes(StandardCharsets.UTF_8))) {
                    log.info("API 키 일치 - 오프셋: {}", offset);
                    return true;
                }
            };
            
            log.warn("모든 시간 윈도우에서 API 키 불일치");
            return false;
        } catch (Exception e) {
            log.error("API 키 검증 중 오류 발생", e);
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        log.warn("API 키 검증 실패 응답 전송: {}", message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
} 