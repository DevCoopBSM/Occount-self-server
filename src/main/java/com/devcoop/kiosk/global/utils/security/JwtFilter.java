package com.devcoop.kiosk.global.utils.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 로그인 경로는 토큰 검증 없이 통과
        if (request.getRequestURI().equals("/kiosk/auth/signIn")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("Authorization: {}", authorization);

        // SecurityConfig에서 이미 permitAll()로 설정된 경로는 토큰이 없어도 통과
        if (isPermitAllPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증이 필요한 경로에서 토큰이 없는 경우
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, "인증 토큰이 필요합니다.");
            return;
        }

        String token = authorization.split(" ")[1];

        // 토큰 만료 검증
        if (JwtUtil.isExpired(token, secretKey)) {
            sendErrorResponse(response, "토큰이 만료되었습니다.");
            return;
        }

        // UserId 토큰에서 추출
        String userCode = JwtUtil.getCodeNumber(token, secretKey);
        
        if (userCode == null || userCode.isBlank()) {
            sendErrorResponse(response, "유효하지 않은 토큰입니다.");
            return;
        }

        // 권한 부여
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(userCode, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAllPath(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        // 로그인 경로만 허용
        return path.equals("/kiosk/auth/signIn") && method.equals("POST");
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}