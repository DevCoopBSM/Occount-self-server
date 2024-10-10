package com.devcoop.kiosk.domain.user.service;

import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.presentation.dto.LoginRequest;
import com.devcoop.kiosk.domain.user.presentation.dto.LoginResponse;
import com.devcoop.kiosk.domain.user.presentation.dto.PinChangeRequest;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;
import com.devcoop.kiosk.global.utils.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


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
    public ResponseEntity<?> login(LoginRequest dto) throws GlobalException {
        String userCode = dto.userCode();
        String userPin = dto.userPin();

        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (!bCryptPasswordEncoder.matches(userPin, user.getUserPin())) {
            throw new RuntimeException("핀 번호가 옳지 않습니다");
        }

        if (userCode.equals(DEFAULT_PIN_CODE) && user.getUserCode().equals(DEFAULT_PIN_CODE)) {
            // 리다이렉션 URI과 메시지를 포함한 응답 생성
            Map<String, String> response = new HashMap<>();
            response.put("message", "안전하지 않은 비밀번호입니다. 비밀번호를 변경해주세요");
            response.put("redirectUrl", "/pin/change");

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        String token = JwtUtil.createJwt(userCode, secretKey, exprTime);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userNumber(user.getUserNumber())
                .userCode(user.getUserCode())
                .userName(user.getUserName())
                .userPoint(user.getUserPoint())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
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
}
