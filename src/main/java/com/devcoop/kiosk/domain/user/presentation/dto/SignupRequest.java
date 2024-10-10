package com.devcoop.kiosk.domain.user.presentation.dto;

public record SignupRequest(
        String userNumber,
        String userName,
        String userCode,
        String userPin,
        String userEmail,
        String userPassword
) {

}
