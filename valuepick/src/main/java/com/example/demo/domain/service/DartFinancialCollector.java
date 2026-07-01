package com.example.demo.domain.service;

import com.example.demo.domain.dart.DartItem;
import com.example.demo.domain.dart.DartResponse;
import com.example.demo.domain.dto.FinancialStatementDto;
import com.example.demo.domain.entity.Company;
import com.example.demo.domain.repository.CompanyRepository;
import com.example.demo.domain.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DartFinancialCollector {

    private final RestTemplate restTemplate;
    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;

    @Value("${dart.api.key}")
    private String apiKey;

    private static final int SLEEP_MS = 100; // DART API 호출 간격 (차단 방지)
    private static final int RETRY_COUNT = 1; // 실패 시 재시도 횟수

    // 3개년(당기·전기·전전기) 수집 — 초기 적재 시 사용
    @Async("dartExecutor")
    public void collect(String year, String reprtCode) {
        doCollect(year, reprtCode, 3);
    }

    // 1개년(당기만) 수집 — 연간 업데이트 시 사용 (API 1번 호출, 당기 데이터만 저장)
    @Async("dartExecutor")
    public void collectCurrentOnly(String year, String reprtCode) {
        doCollect(year, reprtCode, 1);
    }

    private void doCollect(String year, String reprtCode, int yearsToCollect) {

        int savedCount = 0;
        int page = 0;
        final int PAGE_SIZE = 100;

        while (true) {

            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Company> companyPage = companyRepository.findAll(pageable);
            List<Company> companies = companyPage.getContent();

            if (companies.isEmpty()) break;

            for (Company company : companies) {

                try {

                    DartResponse response = requestWithRetry(
                            company.getCorpCode(), year, reprtCode);

                    if (response == null || !"000".equals(response.getStatus())) {
                        log.warn("API 실패: {} - status={}", company.getCorpName(), response != null ? response.getStatus() : "null");
                        continue;
                    }

                    List<FinancialStatementDto> dtos = mapToDtos(company, response.getList(), year, reprtCode, yearsToCollect);
                    for (FinancialStatementDto dto : dtos) {
                        if (financialStatementRepository.findByStockCodeAndYearAndReprtCode(
                                company.getStockCode(), dto.getBsnsYear(), reprtCode).isPresent()) {
                            log.info("이미 존재, 스킵: {} ({}년)", company.getCorpName(), dto.getBsnsYear());
                            continue;
                        }
                        financialStatementRepository.save(dto.toEntity(company));
                        savedCount++;
                    }
                    log.info("저장 완료: {} (page={}, total={})", company.getCorpName(), page, savedCount);

                    Thread.sleep(SLEEP_MS);

                } catch (Exception e) {
                    log.error("처리 실패: {}", company.getCorpName(), e);
                }
            }

            if (!companyPage.hasNext()) break;
            page++;
        }

        log.info("FinancialStatement 저장 완료: {}건 ({}개년)", savedCount, yearsToCollect);
    }

    // DART API 호출 (재시도 포함)
    private DartResponse requestWithRetry(String corpCode, String year, String reprtCode) {

        String url = buildUrl(corpCode, year, reprtCode);

        for (int i = 0; i < RETRY_COUNT; i++) {

            try {
                ResponseEntity<DartResponse> response =
                        restTemplate.getForEntity(url, DartResponse.class);
                return response.getBody();

            } catch (Exception e) {
                log.warn("재시도 {}/{}", i + 1, RETRY_COUNT);
                try {
                    Thread.sleep(500L * (i + 1)); // 재시도마다 대기시간 증가
                } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    // DART 재무 API URL 생성
    private String buildUrl(String corpCode, String year, String reprtCode) {
        return "https://opendart.fss.or.kr/api/fnlttMultiAcnt.json"
                + "?crtfc_key=" + apiKey
                + "&corp_code=" + corpCode
                + "&bsns_year=" + year
                + "&reprt_code=" + reprtCode;
    }

    // API 응답 리스트 → DTO 리스트 변환 (yearsToCollect: 1=당기만, 3=당기·전기·전전기)
    // CFS(연결재무제표) 우선, 없으면 OFS(별도재무제표) 사용
    private List<FinancialStatementDto> mapToDtos(Company company, List<DartItem> items,
                                                   String year, String reprtCode, int yearsToCollect) {
        Map<String, Long> cfsCurr = new HashMap<>(), cfsPrev = new HashMap<>(), cfsPrevPrev = new HashMap<>();
        Map<String, Long> ofsCurr = new HashMap<>(), ofsPrev = new HashMap<>(), ofsPrevPrev = new HashMap<>();
        String cfsCurrency = "KRW", ofsCurrency = "KRW";

        for (DartItem item : items) {
            String name = item.getAccountNm();
            String fsDiv = item.getFsDiv();

            if ("CFS".equals(fsDiv)) {
                if (!cfsCurr.containsKey(name))     cfsCurr.put(name,     parseAmount(item.getAmount()));
                if (!cfsPrev.containsKey(name))      cfsPrev.put(name,     parseAmount(item.getFrmtrm_amount()));
                if (!cfsPrevPrev.containsKey(name))  cfsPrevPrev.put(name, parseAmount(item.getBfefrmtrm_amount()));
                if (item.getCurrency() != null && !item.getCurrency().isBlank()) cfsCurrency = item.getCurrency();
            } else if ("OFS".equals(fsDiv)) {
                if (!ofsCurr.containsKey(name))     ofsCurr.put(name,     parseAmount(item.getAmount()));
                if (!ofsPrev.containsKey(name))      ofsPrev.put(name,     parseAmount(item.getFrmtrm_amount()));
                if (!ofsPrevPrev.containsKey(name))  ofsPrevPrev.put(name, parseAmount(item.getBfefrmtrm_amount()));
                if (item.getCurrency() != null && !item.getCurrency().isBlank()) ofsCurrency = item.getCurrency();
            }
        }

        int y = Integer.parseInt(year);
        List<FinancialStatementDto> all = List.of(
            toDto(company, cfsCurr,     ofsCurr,     cfsCurrency, ofsCurrency, String.valueOf(y),     reprtCode),
            toDto(company, cfsPrev,     ofsPrev,     cfsCurrency, ofsCurrency, String.valueOf(y - 1), reprtCode),
            toDto(company, cfsPrevPrev, ofsPrevPrev, cfsCurrency, ofsCurrency, String.valueOf(y - 2), reprtCode)
        );
        return all.subList(0, Math.min(yearsToCollect, 3));
    }

    private FinancialStatementDto toDto(Company company,
                                         Map<String, Long> cfsMap, Map<String, Long> ofsMap,
                                         String cfsCurrency, String ofsCurrency,
                                         String bsnsYear, String reprtCode) {
        String finalFsDiv = !cfsMap.isEmpty() ? "CFS" : "OFS";
        Map<String, Long> primary  = "CFS".equals(finalFsDiv) ? cfsMap : ofsMap;
        Map<String, Long> fallback = "CFS".equals(finalFsDiv) ? ofsMap : cfsMap;
        String currency = "CFS".equals(finalFsDiv) ? cfsCurrency : ofsCurrency;

        return FinancialStatementDto.builder()
                .bsnsYear(bsnsYear)
                .stockCode(company.getStockCode())
                .reprtCode(reprtCode)
                .fsDiv(finalFsDiv)
                .currency(currency)
                .revenue(pickAny(primary, fallback, "매출액", "영업수익"))
                .operatingIncome(pick(primary, fallback, "영업이익"))
                .netIncome(pickAny(primary, fallback, "당기순이익(손실)", "당기순이익", "당기순손익"))
                .totalAssets(pick(primary, fallback, "자산총계"))
                .totalLiabilities(pick(primary, fallback, "부채총계"))
                .totalEquity(pick(primary, fallback, "자본총계"))
                .build();
    }

    private Long pick(Map<String, Long> primary, Map<String, Long> fallback, String accountNm) {
        if (primary.containsKey(accountNm)) return primary.get(accountNm);
        if (fallback.containsKey(accountNm)) return fallback.get(accountNm);
        return 0L;
    }

    // 여러 계정명 중 처음 매칭되는 값 반환 (기업마다 계정명 표기 다를 때 사용)
    private Long pickAny(Map<String, Long> primary, Map<String, Long> fallback, String... accountNames) {
        for (String name : accountNames) {
            if (primary.containsKey(name)) return primary.get(name);
        }
        for (String name : accountNames) {
            if (fallback.containsKey(name)) return fallback.get(name);
        }
        return 0L;
    }

    // 금액 문자열 → Long 변환 (null, 공백, "-" 처리)
    private Long parseAmount(String amount) {
        if (amount == null || amount.isBlank() || "-".equals(amount.trim())) return 0L;
        return Long.parseLong(amount.replace(",", ""));
    }
}
