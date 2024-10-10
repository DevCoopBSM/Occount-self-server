package com.devcoop.kiosk.domain.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPointRequest(
    @NotBlank(message = "바코드 번호는 필수값입니다") String userCode,
    int totalPrice
) {
    // 추가 로그로 검증
    public UserPointRequest {
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("userCode cannot be null or empty");
        }
    }
}
