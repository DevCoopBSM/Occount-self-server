package com.devcoop.kiosk.domain.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {
    private final UserRepository userRepository;
    
    @Transactional
    public int deductPoints(String userId, int amount) throws GlobalException {
        User user = userRepository.findByUserCode(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        
        if (user.getUserPoint() < amount) {
            throw new GlobalException(ErrorCode.INSUFFICIENT_POINTS);
        }

        int newPoint = user.getUserPoint() - amount;
        user.setUserPoint(newPoint);
        userRepository.save(user);
        return newPoint;
    }
    
    @Transactional
    public int chargePoints(String userId, int amount) throws GlobalException {
        User user = userRepository.findByUserCode(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        
        int newPoint = user.getUserPoint() + amount;
        user.setUserPoint(newPoint);
        userRepository.save(user);
        
        return newPoint;
    }

    @Transactional(readOnly = true)
    public int getUserPoint(String userId) throws GlobalException {
        User user = userRepository.findByUserCode(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        return user.getUserPoint();
    }
} 