package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FINANCIAL_STATEMENT",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bsns_year", "stock_code", "reprt_code", "fs_div"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bsns_year", columnDefinition = "CHAR(4)",unique = true)
    private String bsnsYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code",unique = true)
    private Company company;

    @Column(name = "reprt_code",unique = true)
    private String reprtCode;

    @Column(name = "fs_div",unique = true)
    private String fsDiv;

    private Long revenue;

    @Column(name = "operating_income")
    private Long operatingIncome;

    @Column(name = "net_income")
    private Long netIncome;

    @Column(name = "total_assets")
    private Long totalAssets;

    @Column(name = "total_liabilities")
    private Long totalLiabilities;

    @Column(name = "total_equity")
    private Long totalEquity;
}
