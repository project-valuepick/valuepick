package com.example.demo.domain.controller;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "관심종목", description = "관심종목 등록, 조회, 삭제 API")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @Operation(summary = "관심종목 목록 조회", description = "로그인한 사용자가 등록한 관심종목 목록을 조회한다.")
    @GetMapping
    public List<Map<String, Object>> getFavorites(@AuthenticationPrincipal PrincipalDetails principal) {
        return userFavoriteService.getFavorites(principal.getUserDto().getId());
    }

    @Operation(summary = "관심종목 등록", description = "지정한 종목코드를 로그인한 사용자의 관심종목으로 등록한다.")
    @PostMapping("/{stockCode}")
    public void addFavorite(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "종목코드", example = "005930") @PathVariable String stockCode) {
        userFavoriteService.addFavorite(principal.getUserDto().getId(), stockCode);
    }

    @Operation(summary = "관심종목 삭제", description = "지정한 종목코드를 로그인한 사용자의 관심종목에서 삭제한다.")
    @DeleteMapping("/{stockCode}")
    public void removeFavorite(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "종목코드", example = "005930") @PathVariable String stockCode) {
        userFavoriteService.removeFavorite(principal.getUserDto().getId(), stockCode);
    }
}
