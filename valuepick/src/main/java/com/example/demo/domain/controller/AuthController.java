package com.example.demo.domain.controller;

import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.domain.dto.request.LoginRequest;
import com.example.demo.domain.dto.request.RegisterRequest;
import com.example.demo.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급(refresh) API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 회원을 등록한다.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getEmail(), request.getPassword(), request.getNickname());
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 액세스/리프레시 토큰을 발급받는다.")
    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequest request) {
        TokenInfo tokenInfo = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(tokenInfo);
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰(refreshToken)으로 새로운 JWT 액세스/리프레시 토큰을 재발급받는다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenInfo> refresh(@RequestBody java.util.Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        TokenInfo tokenInfo = authService.refresh(refreshToken);
        return ResponseEntity.ok(tokenInfo);
    }

}
