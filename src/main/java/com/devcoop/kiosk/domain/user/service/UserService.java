package com.devcoop.kiosk.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.presentation.dto.UserInfoResponse;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String userCode) throws GlobalException {
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.builder()
                .userName(user.getUserName())
                .userCode(user.getUserCode())
                .userPoint(user.getUserPoint())
                .build();
    }
} 