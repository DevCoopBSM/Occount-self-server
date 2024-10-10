package com.devcoop.kiosk.domain.user.presentation.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String token,
        String userNumber,
        String userCode,
        String userName,
        int userPoint  // point -> userPoint
) {

}
