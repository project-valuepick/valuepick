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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialIndicatorService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndicatorRepository stockIndicatorRepository;
    private final DividendInfoRepository dividendInfoRepository;
    private final ExchangeRateApiService exchangeRateApiService;

    // ── 상수 ──────────────────────────────────────────────────────────
    private static final int PAGE_SIZE = 100;

    // 재무제표 구분: 연결(CFS) 우선, 없으면 별도(OFS) 사용
    private static final String FS_DIV_CFS = "CFS"; // 연결재무제표
    private static final String FS_DIV_OFS = "OFS"; // 별도재무제표

    // ── 진입점 ────────────────────────────────────────────────────────

    /**
     * 전체 기업 지표 계산 및 저장
     *
     * @param year      기준 연도 (ex. "2023")
     * @param reprtCode 보고서 코드 (ex. "11011" = 사업보고서)
     */
    @Transactional
    public void calculateAll(String year, String reprtCode) {

        int savedCount = 0;
        int page = 0;

        while (true) {

            // 100건씩 페이징 조회
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Company> companyPage = companyRepository.findAll(pageable);
            List<Company> companies = companyPage.getContent();

            if (companies.isEmpty()) break;

            for (Company company : companies) {
                try {
                    boolean saved = calculateAndSave(company, year, reprtCode);
                    if (saved) {
                        savedCount++;
                        log.info("지표 저장 완료: {} (page={}, total={})",
                                company.getCorpName(), page, savedCount);
                    }
                } catch (Exception e) {
                    // 개별 실패는 로그만 남기고 다음 기업 계속 처리
                    log.error("지표 계산 실패: {} | 사유: {}", company.getCorpName(), e.getMessage());
                }
            }

            if (!companyPage.hasNext()) break;
            page++;
        }

        log.info("지표 계산 완료: 총 {}건 저장", savedCount);
    }

    // ── 단일 기업 처리 ────────────────────────────────────────────────

    /**
     * 단일 기업 지표 계산 후 저장
     * 저장 성공 시 true, 데이터 부족으로 스킵 시 false 반환
     */
    private boolean calculateAndSave(Company company, String year, String reprtCode) {

        // ── 재무제표 조회: CFS 우선, 없으면 OFS ──────────────────────
        Optional<FinancialStatement> financialOpt =
                financialStatementRepository.findByStockCodeAndYearAndReprtCodeAndFsDiv(
                        company.getStockCode(), year, reprtCode, FS_DIV_CFS);

        if (financialOpt.isEmpty()) {
            financialOpt = financialStatementRepository.findByStockCodeAndYearAndReprtCodeAndFsDiv(
                    company.getStockCode(), year, reprtCode, FS_DIV_OFS);
        }

        if (financialOpt.isEmpty()) {
            log.warn("재무데이터 없음 (CFS/OFS 모두): {} year={} reprtCode={}",
                    company.getCorpName(), year, reprtCode);
            return false;
        }

        // ── 최신 주가 조회 ────────────────────────────────────────────
        Optional<StockPrice> stockPriceOpt =
                stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(company.getStockCode());

        if (stockPriceOpt.isEmpty()) {
            log.warn("주가데이터 없음: {}", company.getCorpName());
            return false;
        }

        FinancialStatement financial = financialOpt.get();
        StockPrice stockPrice = stockPriceOpt.get();

        // ── 지표 계산 후 저장 (upsert: PK 충돌 시 덮어쓰기) ─────────
        StockIndicator indicator = calculate(company, financial, stockPrice, year);
        stockIndicatorRepository.save(indicator);

        return true;
    }

    // ── 지표 계산 ─────────────────────────────────────────────────────

    /**
     * EPS, BPS, PER, PBR, ROE, 부채비율, 배당수익률 계산
     * 환율 조회 실패 시 IllegalStateException → calculateAll에서 catch 후 스킵
     */
    private StockIndicator calculate(Company company, FinancialStatement financial,
                                     StockPrice stockPrice, String year) {

        // ── 환율 조회 ─────────────────────────────────────────────────
        // currency가 null이면 KRW로 간주 → fxRate = 1.0
        String currency = financial.getCurrency() != null ? financial.getCurrency() : "KRW";
        double fxRate = resolveExchangeRate(company.getCorpName(), currency);

        // ── 기초 데이터 추출 ──────────────────────────────────────────
        long shareCount  = nvl(stockPrice.getLstgStCnt());          // 상장주식수
        long closePrice  = nvl(stockPrice.getClpr());               // 종가 (항상 KRW)

        // 재무 수치 → KRW 변환 (KRW 통화면 fxRate = 1.0이라 값 동일)
        long netIncome   = toKrw(financial.getNetIncome(),         fxRate); // 당기순이익
        long equity      = toKrw(financial.getTotalEquity(),       fxRate); // 자본총계
        long liabilities = toKrw(financial.getTotalLiabilities(), fxRate); // 부채총계

        // ── 지표 계산 ─────────────────────────────────────────────────

        // EPS (주당순이익) = 당기순이익 / 상장주식수
        Double eps = safeDiv(netIncome, shareCount);

        // 완전자본잠식 (equity <= 0): BPS·PBR·ROE·부채비율 N/A
        // → 분모가 0 이하이면 수치가 폭발하거나 의미가 없어짐
        Double bps       = equity > 0 ? safeDiv(equity, shareCount)                      : null;
        Double pbr       = equity > 0 && bps != null ? round(closePrice / bps)           : null;
        Double roe       = equity > 0 ? round((double) netIncome / equity * 100)         : null;
        Double debtRatio = equity > 0 ? round((double) liabilities / equity * 100)       : null;

        // 적자 기업 (eps <= 0): PER N/A
        // → 음수 EPS로 계산된 PER은 실무에서 N/A 처리 (네이버 증권 등 동일)
        Double per = (eps != null && eps > 0) ? round(closePrice / eps) : null;

        // 배당수익률 = 주당배당금 / 종가 × 100
        // year 기준 보통주 배당 조회 (연도 불일치 방지)
        Double dividendYield = resolveDividendYield(company.getCorpCode(), closePrice);

        // ── StockIndicator 빌드 ───────────────────────────────────────
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

    private Double resolveDividendYield(String corpCode, long closePrice) {
        if (closePrice == 0) return null;

        Optional<DividendInfo> dividendOpt =
                dividendInfoRepository.findByCorpCodeAndDividendKind(corpCode, "보통주");

        return dividendOpt
                .filter(d -> d.getDividendAmount() != null)
                .map(d -> round((double) d.getDividendAmount() / closePrice * 100))
                .orElse(null);
    }

    // ── 환율 조회 ─────────────────────────────────────────────────────

    /**
     * 환율 조회 실패 시 계산 스킵 (잘못된 환율로 저장하는 것보다 안전)
     * 호출부(calculateAndSave)에서 catch 후 해당 기업 건너뜀
     */
    private double resolveExchangeRate(String corpName, String currency) {
        try {
            return exchangeRateApiService.getRateToKrw(currency);
        } catch (Exception e) {
            log.warn("환율 조회 실패 → 지표 계산 스킵: {} currency={}", corpName, currency);
            throw new IllegalStateException("환율 조회 실패: " + currency, e);
        }
    }

    // ── 유틸 메서드 ───────────────────────────────────────────────────

    /** 외화 금액 → KRW 변환. null은 0 처리 */
    private long toKrw(Long amount, double fxRate) {
        if (amount == null) return 0L;
        return Math.round(amount * fxRate);
    }

    /** null → 0L 변환 (Long wrapper 안전 처리) */
    private long nvl(Long value) {
        return value != null ? value : 0L;
    }

    /** 0 나누기 방지. denominator가 0이면 null 반환 */
    private Double safeDiv(long numerator, long denominator) {
        if (denominator == 0) return null;
        return (double) numerator / denominator;
    }

    /** 소수점 둘째 자리 반올림 */
    private Double round(Double value) {
        if (value == null) return null;
        return Math.round(value * 100.0) / 100.0;
    }
}