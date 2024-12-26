package com.devcoop.kiosk.domain.pg.presentation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.devcoop.kiosk.domain.payment.dto.PaymentRequest;
import com.devcoop.kiosk.domain.payment.dto.PaymentResponse;
import com.devcoop.kiosk.domain.payment.service.PaymentService;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kiosk", description = "Kiosk API")
public class PgController {
    private final PaymentService selfCounterService;

    @PostMapping("/executePayments")
    @Operation(summary = "kiosk service", description = "키오스크 결제 처리 API")
    public ResponseEntity<?> executeTransactions(
        @RequestBody PaymentRequest request,
        Authentication authentication
    ) {
        try {
            String userEmail = authentication.getName();
            PaymentResponse response = selfCounterService.executeAllTransactions(request, userEmail);
            return ResponseEntity.ok(response);
            
        } catch (GlobalException e) {
            log.error("결제 처리 중 오류: {}", e.getMessage());
            HttpStatus status = determineHttpStatus(e.getErrorCode());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("code", e.getErrorCode().name());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
            
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case PAYMENT_TIMEOUT -> HttpStatus.REQUEST_TIMEOUT;
            case INVALID_PAYMENT_REQUEST -> HttpStatus.BAD_REQUEST;
            case TRANSACTION_IN_PROGRESS -> HttpStatus.CONFLICT;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_POINTS -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
