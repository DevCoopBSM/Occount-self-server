package com.devcoop.kiosk.domain.user.presentation.dto;

import lombok.Builder;

@Builder
public record PinChangeRequest(
        String userCode, // codeNumber -> userCode
        String userPin,  // pin -> userPin
        String newPin  // newPin -> newPin
){

}
