package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Company;
import com.example.demo.domain.entity.FinancialStatement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재무제표 정보")
public class FinancialStatementDto {

    @Schema(description = "ID", example = "1")
    private Long id;
    @Schema(description = "사업연도", example = "2025")
    private String bsnsYear;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "보고서 코드 (11011=사업보고서)", example = "11011")
    private String reprtCode;
    @Schema(description = "개별/연결 구분", example = "CFS")
    private String fsDiv;
    @Schema(description = "매출액")
    private Long revenue;
    @Schema(description = "영업이익")
    private Long operatingIncome;
    @Schema(description = "당기순이익")
    private Long netIncome;
    @Schema(description = "자산총계")
    private Long totalAssets;
    @Schema(description = "부채총계")
    private Long totalLiabilities;
    @Schema(description = "자본총계")
    private Long totalEquity;
    @Schema(description = "유동자산")
    private Long currentAssets;
    @Schema(description = "유동부채")
    private Long currentLiabilities;
    @Schema(description = "영업활동현금흐름")
    private Long operatingCashFlow;
    @Schema(description = "매출총이익")
    private Long grossProfit;
    @Schema(description = "통화", example = "KRW")
    private String currency;


    public FinancialStatement toEntity(Company company) {
        return FinancialStatement.builder()
                .bsnsYear(this.bsnsYear)
                .company(company)
                .reprtCode(this.reprtCode)
                .fsDiv(this.fsDiv)
                .revenue(this.revenue)
                .operatingIncome(this.operatingIncome)
                .netIncome(this.netIncome)
                .totalAssets(this.totalAssets)
                .totalLiabilities(this.totalLiabilities)
                .totalEquity(this.totalEquity)
                .currentAssets(this.currentAssets)
                .currentLiabilities(this.currentLiabilities)
                .operatingCashFlow(this.operatingCashFlow)
                .grossProfit(this.grossProfit)
                .currency(this.currency)
                .build();
    }

    public static FinancialStatementDto from(FinancialStatement entity) {
        return FinancialStatementDto.builder()
                .id(entity.getId())
                .bsnsYear(entity.getBsnsYear())
                .stockCode(entity.getCompany().getStockCode())
                .reprtCode(entity.getReprtCode())
                .fsDiv(entity.getFsDiv())
                .revenue(entity.getRevenue())
                .operatingIncome(entity.getOperatingIncome())
                .netIncome(entity.getNetIncome())
                .totalAssets(entity.getTotalAssets())
                .totalLiabilities(entity.getTotalLiabilities())
                .totalEquity(entity.getTotalEquity())
                .currentAssets(entity.getCurrentAssets())
                .currentLiabilities(entity.getCurrentLiabilities())
                .operatingCashFlow(entity.getOperatingCashFlow())
                .grossProfit(entity.getGrossProfit())
                .currency(entity.getCurrency())
                .build();
    }
}
