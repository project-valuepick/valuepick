package com.example.demo.domain.controller;

import com.example.demo.domain.service.FinancialIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "투자지표 계산 (관리자)", description = "재무제표 데이터를 기반으로 투자지표(PER, PBR, ROE 등)를 계산하는 관리자 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/indicator")
public class FinancialIndicatorController {

    private final FinancialIndicatorService financialIndicatorService;

    @Operation(summary = "투자지표 계산", description = "지정한 연도(year)와 보고서 코드(reprtCode)를 기준으로 재무제표 데이터를 조회하여 전체 회사의 투자지표를 계산하고 저장한다.")
    @GetMapping("/calculate/{year}/{reprtCode}")
    public ResponseEntity<String> calculate(@PathVariable String year,@PathVariable String reprtCode) {
        log.info("지표 계산 요청: year={}", year);
        financialIndicatorService.calculateAll(year,reprtCode);
        return ResponseEntity.ok(year + "년 지표 계산 완료");
    }
}
