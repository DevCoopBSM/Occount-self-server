package com.devcoop.kiosk.global.exception;

import com.devcoop.kiosk.global.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
