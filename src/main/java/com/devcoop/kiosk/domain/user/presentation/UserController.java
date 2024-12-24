package com.devcoop.kiosk.domain.user.presentation;

import com.devcoop.kiosk.domain.user.presentation.dto.UserInfoResponse;
import com.devcoop.kiosk.domain.user.service.UserService;
import com.devcoop.kiosk.global.exception.GlobalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class UserController {
    private final UserService userService;

    @GetMapping("/user-info")
    @Operation(summary = "get user info", description = "사용자 정보 조회")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) throws GlobalException {
        String userCode = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserInfo(userCode));
    }
} 