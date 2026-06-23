package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Company;
import com.example.demo.domain.entity.FinancialStatement;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatementDto {

    private Long id;
    private String bsnsYear;
    private String stockCode;
    private String reprtCode;
    private String fsDiv;
    private Long revenue;
    private Long operatingIncome;
    private Long netIncome;
    private Long totalAssets;
    private Long totalLiabilities;
    private Long totalEquity;

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
                .build();
    }
}
