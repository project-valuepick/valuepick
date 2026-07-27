package com.example.demo.domain.controller;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.service.ExchangeRateApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "환율 수집 (관리자)", description = "환율 데이터 수집 및 전일 대비 등락률 계산 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/exchange")
public class ExchangeController {

    private final ExchangeRateApiService exchangeRateApiService;

    @Operation(summary = "특정 날짜 환율 수집", description = "지정한 날짜(date, yyyyMMdd 형식, 예: 20260621)의 환율 데이터를 수집하여 저장한다.")
    @GetMapping("/collect/{date}")
    public ResponseEntity<List<ExchangeDto>> collect(
            @Parameter(description = "수집 기준일 (yyyyMMdd)", example = "20260621") @PathVariable String date) {
        List<ExchangeDto> result = exchangeRateApiService.fetchAndSaveExchangeRates(date);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "오늘 날짜 환율 수집", description = "오늘 날짜 기준 환율 데이터를 수집하여 저장한다.")
    @GetMapping("/collect/today")
    public ResponseEntity<List<ExchangeDto>> collectToday() {
        List<ExchangeDto> result = exchangeRateApiService.fetchAndSaveExchangeRatesForToday();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "환율 등락률 계산", description = "지정한 날짜(date, yyyyMMdd 형식, 예: 20260621)의 환율을 전일 대비 등락률/등락폭으로 계산하여 저장한다.")
    @GetMapping("/changes/{date}")
    public ResponseEntity<List<ExchangeDto>> getChanges(
            @Parameter(description = "계산 기준일 (yyyyMMdd)", example = "20260621") @PathVariable String date) {
        List<ExchangeDto> result = exchangeRateApiService.getExchangeRateChanges(date);
        return ResponseEntity.ok(result);
    }
}