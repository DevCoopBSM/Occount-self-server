package com.devcoop.kiosk.domain.user.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.presentation.dto.LoginRequest;
import com.devcoop.kiosk.domain.user.presentation.dto.LoginResponse;
import com.devcoop.kiosk.domain.user.presentation.dto.PinChangeRequest;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;
import com.devcoop.kiosk.global.utils.security.JwtUtil;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserAuthService {

//    private static final Logger logger = LoggerFactory.getLogger(UserAuthService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final Long exprTime = 1000 * 60 * 30L;
    private static final String DEFAULT_PIN_CODE = "1234";

    @Value("${jwt.secret}")
    private String secretKey;

    @Transactional
    public ResponseEntity<?> login(LoginRequest dto) {
        String userCode = dto.userCode();
        String userPin = dto.userPin();

        // 사용자 찾기
        User user = userRepository.findByUserCode(userCode)
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", ErrorCode.USER_NOT_FOUND.name());
                    response.put("message", ErrorCode.USER_NOT_FOUND.getMessage());
                    return null;
                });
        
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", ErrorCode.USER_NOT_FOUND.name());
            response.put("message", ErrorCode.USER_NOT_FOUND.getMessage());
            return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getStatus()).body(response);
        }

        // PIN 번호 확인
        if (!bCryptPasswordEncoder.matches(userPin, user.getUserPin())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", ErrorCode.INVALID_PIN.name());
            response.put("message", ErrorCode.INVALID_PIN.getMessage());
            return ResponseEntity.status(ErrorCode.INVALID_PIN.getStatus()).body(response);
        }

        // 초기 비밀번호 체크
        if (userPin.equals(DEFAULT_PIN_CODE)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", ErrorCode.DEFAULT_PIN_IN_USE.name());
            response.put("message", ErrorCode.DEFAULT_PIN_IN_USE.getMessage());
            return ResponseEntity.status(ErrorCode.DEFAULT_PIN_IN_USE.getStatus()).body(response);
        }

        String token = JwtUtil.createJwt(userCode, secretKey, exprTime);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userNumber(user.getUserNumber())
                .userCode(user.getUserCode())
                .userName(user.getUserName())
                .userPoint(user.getUserPoint())
                .build();

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> changePassword(PinChangeRequest dto) throws GlobalException {
        String userCode = dto.userCode(); // codeNumber를 userCode로 변경
        String userPin = dto.userPin(); // pin을 userPin으로 변경
        String newPin = dto.newPin();


        Map<String, String> response = new HashMap<>();
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND)); // 메서드 호출 수정

        if (!bCryptPasswordEncoder.matches(userPin, user.getUserPin())) {
            throw new RuntimeException("현재 핀 번호가 옳지 않습니다");
        }

        if (bCryptPasswordEncoder.matches(newPin, user.getUserPin())) {
            throw new RuntimeException("현재 핀번호와 다른 핀번호를 입력해주세요");
        }

        String encodedPin = bCryptPasswordEncoder.encode(newPin);

        user.changePin(encodedPin); // update 메서드를 changePin으로 변경
        userRepository.save(user);

        response.put("message", "성공적으로 비밀번호를 변경하였습니다");
        response.put("redirectUrl", "/");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public void resetPassword(String userCode) throws GlobalException {

        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));// 메서드 호출 수정

        user.changePin(bCryptPasswordEncoder.encode(DEFAULT_PIN_CODE)); // update 메서드를 changePin으로 변경
        userRepository.save(user);
    }

    public User getUserFromToken(String token) throws GlobalException {
        // "Bearer " 제거
        String actualToken = token.replace("Bearer ", "");
        
        // 토큰에서 userCode 추출
        String userCode = JwtUtil.getCodeNumber(actualToken, secretKey);
        if (userCode == null) {
            throw new GlobalException(ErrorCode.INVALID_TOKEN);
        }
        
        // userCode로 사용자 조회
        return userRepository.findByUserCode(userCode)
            .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }
}
