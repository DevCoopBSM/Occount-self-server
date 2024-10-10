package com.devcoop.kiosk.domain.user.service;

import com.devcoop.kiosk.domain.user.presentation.dto.UserPointRequest;
import com.devcoop.kiosk.global.exception.GlobalException;

public interface UserPointService {
    Object deductPoints(UserPointRequest requestDto) throws GlobalException;
}
