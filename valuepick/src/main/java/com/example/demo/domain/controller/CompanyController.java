package com.example.demo.domain.controller;

import com.example.demo.domain.service.DartCompanyCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final DartCompanyCollector collector;

    // DART에서 전체 상장 기업 목록 수집 후 Company 테이블에 저장
    // 기존: DartStockCollector(발행주식수 수집)는 lstgStCnt(상장주식수)로 대체되어 제거됨
    @GetMapping("/company/load")
    public String load() {
        collector.collectCompanies();
        return "완료";
    }

}
