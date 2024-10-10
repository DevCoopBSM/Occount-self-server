package com.devcoop.kiosk.domain.user.presentation.dto;

import lombok.Builder;

@Builder
public record LoginRequest(
        String userCode, // codeNumber -> userCode
        String userPin  // pin -> userPin
) {

}
