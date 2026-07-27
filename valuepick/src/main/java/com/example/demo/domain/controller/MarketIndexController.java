package com.example.demo.domain.controller;

import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.service.MarketIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "시장지수 수집 (관리자)", description = "코스피 등 시장지수 데이터를 수집하여 저장하는 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/market")
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    @Operation(summary = "특정 날짜 코스피 지수 수집", description = "지정한 날짜(basDd, yyyyMMdd 형식, 예: 20260613)의 코스피 지수를 수집하여 저장한다.")
    @GetMapping("/collect/{basDd}")
    public ResponseEntity<List<MarketIndexDto>> collect(
            @Parameter(description = "수집 기준일 (yyyyMMdd)", example = "20260613") @PathVariable String basDd) {
        List<MarketIndexDto> result = marketIndexService.fetchAndSave(basDd);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "오늘 날짜 코스피 지수 수집", description = "오늘 날짜 기준 코스피 지수를 수집하여 저장한다.")
    @GetMapping("/collect/today")
    public ResponseEntity<List<MarketIndexDto>> collectToday() {
        List<MarketIndexDto> result = marketIndexService.fetchAndSaveForToday();
        return ResponseEntity.ok(result);
    }
}