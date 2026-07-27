package com.example.demo.domain.controller;

import com.example.demo.domain.service.DartFinancialCollector;
import com.example.demo.domain.service.DividendCollector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DART 수집 (관리자)", description = "DART 재무제표, 배당 데이터를 수집하여 저장하는 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dart")
public class DartCollectorController {

    private final DartFinancialCollector dartFinancialCollector;
    private final DividendCollector dividendCollector;

    @Operation(summary = "DART 재무제표 수집", description = "지정한 연도(year)와 보고서 코드(reportCode, 기본값 11011)의 DART 재무제표 데이터를 수집한다. 11011=사업보고서, 11012=반기보고서, 11013=1분기, 11014=3분기.")
    @GetMapping("/financial/{year}")
    public String collectFinancial(
            @Parameter(description = "수집 대상 연도", example = "2025") @PathVariable String year,
            @Parameter(description = "보고서 코드 (11011=사업보고서, 11012=반기, 11013=1분기, 11014=3분기)", example = "11011") @RequestParam(defaultValue = "11011") String reportCode
    ) {
        dartFinancialCollector.collect(year, reportCode);
        return "Financial 데이터 수집 완료";
    }

    @Operation(summary = "DART 배당 데이터 수집", description = "지정한 연도(year)와 보고서 코드(reportCode, 기본값 11011)의 DART 배당 데이터를 수집한다.")
    @GetMapping("/dividend/{year}")
    public String collectDividend(
            @Parameter(description = "수집 대상 연도", example = "2025") @PathVariable String year,
            @Parameter(description = "보고서 코드 (11011=사업보고서)", example = "11011") @RequestParam(defaultValue = "11011") String reportCode
    ) {
        dividendCollector.collect(year, reportCode);
        return "Dividend 데이터 수집 완료";
    }
}
