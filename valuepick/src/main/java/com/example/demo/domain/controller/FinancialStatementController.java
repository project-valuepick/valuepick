package com.example.demo.domain.controller;

import com.example.demo.domain.dto.FinancialStatementDto;
import com.example.demo.domain.service.FinancialStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "재무제표", description = "종목별 재무제표 조회 API")
@RestController
@RequestMapping("/api/stocks/{stockCode}/financial-statements")
@RequiredArgsConstructor
public class FinancialStatementController {

    private final FinancialStatementService financialStatementService;

    @Operation(summary = "종목별 재무제표 조회", description = "지정한 종목코드(stockCode)의 재무제표 목록을 조회한다.")
    @GetMapping
    public List<FinancialStatementDto> getFinancialStatements(
            @Parameter(description = "종목코드", example = "005930") @PathVariable String stockCode) {
        return financialStatementService.getFinancialStatements(stockCode);
    }
}
