package com.devcoop.kiosk.domain.user.presentation.dto;

import lombok.Builder;

@Builder
public record UserInfoResponse(
    String userName,
    String userCode,
    int userPoint
) {} 