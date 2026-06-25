package com.example.demo.domain.service;

import com.example.demo.domain.entity.Company;
import com.example.demo.domain.entity.DividendInfo;
import com.example.demo.domain.entity.FinancialStatement;
import com.example.demo.domain.entity.StockIndicator;
import com.example.demo.domain.entity.StockPrice;
import com.example.demo.domain.repository.CompanyRepository;
import com.example.demo.domain.repository.DividendInfoRepository;
import com.example.demo.domain.repository.FinancialStatementRepository;
import com.example.demo.domain.repository.StockIndicatorRepository;
import com.example.demo.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialIndicatorService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository; // Financial → FinancialStatement
    private final StockPriceRepository stockPriceRepository;
    private final StockIndicatorRepository stockIndicatorRepository;         // FinancialIndicator → StockIndicator
    private final DividendInfoRepository dividendInfoRepository;             // DividendRepository → DividendInfoRepository

    public void calculateAll(int year, String reprtCode) {

        int savedCount = 0;
        int page = 0;
        final int PAGE_SIZE = 100;

        while (true) {

            // 100건씩 페이징해서 Company 조회
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Company> companyPage = companyRepository.findAll(pageable);
            List<Company> companies = companyPage.getContent();

            if (companies.isEmpty()) break;

            for (Company company : companies) {

                try {

                    // 해당 연도 재무제표 조회 - bsnsYear가 String이므로 String.valueOf()로 변환
                    Optional<FinancialStatement> financialOpt =
                            financialStatementRepository.findByStockCodeAndYearAndReprtCode(
                                    company.getStockCode(), String.valueOf(year), reprtCode);

                    if (financialOpt.isEmpty()) {
                        log.warn("재무데이터 없음: {}", company.getCorpName());
                        continue;
                    }

                    // 가장 최신 주가 조회 - srtnCd 기준으로 basDt 내림차순 정렬
                    Optional<StockPrice> stockPriceOpt =
                            stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(company.getStockCode());

                    if (stockPriceOpt.isEmpty()) {
                        log.warn("주가데이터 없음: {}", company.getCorpName());
                        continue;
                    }

                    FinancialStatement financial = financialOpt.get();
                    StockPrice stockPrice = stockPriceOpt.get();

                    // 지표 계산 후 StockIndicator 저장
                    StockIndicator indicator = calculate(company, financial, stockPrice);

                    // StockIndicator PK는 stockCode - 이미 있으면 덮어쓰기 (upsert)
                    stockIndicatorRepository.save(indicator);
                    savedCount++;
                    log.info("지표 저장 완료: {} (page={}, total={})", company.getCorpName(), page, savedCount);

                } catch (Exception e) {
                    log.error("지표 계산 실패: {}", company.getCorpName(), e);
                }
            }

            if (!companyPage.hasNext()) break;
            page++;
        }

        log.info("지표 계산 완료: {}건", savedCount);
    }

    private StockIndicator calculate(Company company, FinancialStatement financial, StockPrice stockPrice) {

        // ── 기초 데이터 추출 ──────────────────────────────────────────

        // 상장주식수 - 기존 company.getIstcTotqy() 대신 StockPrice.lstgStCnt 사용
        long shareCount = stockPrice.getLstgStCnt() != null ? stockPrice.getLstgStCnt() : 0L;

        long closePrice      = stockPrice.getClpr() != null ? stockPrice.getClpr() : 0L; // 종가 (clpr)
        long netIncome       = financial.getNetIncome() != null ? financial.getNetIncome() : 0L;       // 당기순이익
        long equity          = financial.getTotalEquity() != null ? financial.getTotalEquity() : 0L;   // 자본총계
        long liabilities     = financial.getTotalLiabilities() != null ? financial.getTotalLiabilities() : 0L; // 부채총계

        // ── EPS (주당순이익) = 당기순이익 / 상장주식수 ───────────────
        Double eps = safeDiv(netIncome, shareCount);

        // ── BPS (주당순자산) = 자본총계 / 상장주식수 ─────────────────
        Double bps = safeDiv(equity, shareCount);

        // ── PER (주가수익비율) = 종가 / EPS ──────────────────────────
        Double per = (eps != null && eps != 0) ? round(closePrice / eps) : null;

        // ── PBR (주가순자산비율) = 종가 / BPS ────────────────────────
        Double pbr = (bps != null && bps != 0) ? round(closePrice / bps) : null;

        // ── ROE (자기자본이익률) = 당기순이익 / 자본총계 × 100 ───────
        Double roe = (equity != 0) ? round((double) netIncome / equity * 100) : null;

        // ── 부채비율 = 부채총계 / 자본총계 × 100 ────────────────────
        Double debtRatio = (equity != 0) ? round((double) liabilities / equity * 100) : null;

        // ── 배당수익률 - DividendInfo에서 corpCode 기준으로 조회 ──────
        // 기존 Dividend(복합키, 보통주/우선주 분리) → DividendInfo(corpCode 단일 PK)로 변경
        Optional<DividendInfo> dividendOpt =
                dividendInfoRepository.findByCorpCodeAndDividendKind(company.getCorpCode(), "보통주");

        // 주당배당금 / 종가 × 100 으로 배당수익률 계산
        Double dividendYield = dividendOpt
                .filter(d -> d.getDividendAmount() != null && closePrice != 0)
                .map(d -> round((double) d.getDividendAmount() / closePrice * 100))
                .orElse(null);

        // ── StockIndicator 저장 - 기존 FinancialIndicator와 필드 매핑 ──
        // stockCode = PK, company = @OneToOne 연관 (insertable=false, updatable=false)
        // operatingProfitMargin 필드는 새 엔티티에 없으므로 제거
        // marketCap 필드도 새 엔티티에 없으므로 제거
        return StockIndicator.builder()
                .stockCode(company.getStockCode())
                .eps(eps != null ? round(eps) : null)
                .bps(bps != null ? round(bps) : null)
                .per(per)
                .pbr(pbr)
                .roe(roe)
                .debtRatio(debtRatio)
                .dividendYield(dividendYield)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    // 소수점 둘째자리 반올림
    private Double round(Double value) {
        if (value == null) return null;
        return Math.round(value * 100.0) / 100.0;
    }

    // 0 나누기 방지 유틸
    private Double safeDiv(long numerator, long denominator) {
        if (denominator == 0) return null;
        return (double) numerator / denominator;
    }
}
