package com.example.demo.domain.controller;

import com.example.demo.domain.service.Top100Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TOP100", description = "가치투자 스코어 상위 종목 조회 및 스코어 계산 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/top100")
public class Top100Controller {

    private final Top100Service top100Service;

    @Operation(summary = "TOP100 스코어 재계산 (관리자)", description = "관리자용으로 TOP100 스코어 계산을 수동으로 트리거한다.")
    @PostMapping("/admin/calculate")
    public ResponseEntity<String> calculate() {
        log.info("[Top100Controller] 수동 TOP100 스코어 계산 요청");
        top100Service.calculateAndSave();
        return ResponseEntity.ok("TOP100 스코어 계산 완료");
    }
}