package com.example.demo.domain.controller;

import com.example.demo.domain.dto.request.UpdateNicknameRequest;
import com.example.demo.domain.dto.request.UpdatePasswordRequest;
import com.example.demo.domain.dto.response.UserResponse;
import com.example.demo.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 정보", description = "내 정보 조회, 닉네임/비밀번호 수정, 회원 탈퇴 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자 본인의 정보를 조회한다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMe(userDetails.getUsername()));
    }

    @Operation(summary = "닉네임 수정", description = "로그인한 사용자 본인의 닉네임을 수정한다.")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest request) {
        return ResponseEntity.ok(userService.updateNickname(userDetails.getUsername(), request));
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자 본인의 비밀번호를 변경한다.")
    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원탈퇴", description = "로그인한 사용자 본인의 계정을 탈퇴 처리한다.")
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
