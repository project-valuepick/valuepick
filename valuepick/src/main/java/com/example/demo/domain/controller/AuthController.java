package com.example.demo.domain.controller;

import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.domain.dto.request.LoginRequest;
import com.example.demo.domain.dto.request.RegisterRequest;
import com.example.demo.domain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getEmail(), request.getPassword(), request.getNickname());
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequest request) {
        TokenInfo tokenInfo = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(tokenInfo);
    }

    // 임시 예외처리 - 추후 GlobalExceptionHandler 로 이전 예정
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
