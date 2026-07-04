package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "COMPANY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "corp_code", unique = true)
    private String corpCode;

    @Column(name = "corp_name")
    private String corpName;

    @Column(name = "corp_cls", columnDefinition = "CHAR(1)")
    private String corpCls;

    // DART company.json의 induty_code (표준산업분류코드, 예: 삼성전자 "264") - 업종명은 API 미제공
    @Column(name = "induty_code", length = 10)
    private String indutyCode;

    // induty_code 앞 2자리(KSIC 중분류)를 DartCompanyCollector의 매핑표로 변환한 값
    @Column(name = "induty_nm")
    private String indutyNm;

    @Column(name = "ceo_nm")
    private String ceoNm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FinancialStatement> financialStatements;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StockPrice> stockPrices;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StockIndicator stockIndicator;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DividendInfo> dividendInfos;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Top100> top100List;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserFavorite> userFavorites;

    // DartCompanyCollector 5단계에서 DART company.json 조회 후 업종코드·업종명·대표자명 반영할 때 사용
    public void setIndustryInfo(String indutyCode, String indutyNm, String ceoNm) {
        this.indutyCode = indutyCode;
        this.indutyNm = indutyNm;
        this.ceoNm = ceoNm;
        this.updatedAt = LocalDateTime.now();
    }
}
