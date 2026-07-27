package com.example.demo.domain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.service.DartCompanyCollector;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "기업 수집 (관리자)", description = "DART 기업 개황 데이터를 수집하여 저장하는 관리자 API")
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final DartCompanyCollector collector;

    @Operation(summary = "DART 기업 개황 수집", description = "지정한 기준일자(basDt, yyyyMMdd 형식)의 DART 기업 개황 데이터를 비동기로 수집하여 저장한다.")
    @GetMapping("/company/load")
    public ResponseEntity<String> load(
            @Parameter(description = "수집 기준일자 (yyyyMMdd)", example = "20260101") @RequestParam String basDt) {
        if (basDt == null || !basDt.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body("날짜 형식이 올바르지 않습니다. yyyyMMdd 형식으로 입력하세요.");
        }
        collector.collectCompanies(basDt);
        return ResponseEntity.ok("수집 시작 (비동기)");
    }

}
