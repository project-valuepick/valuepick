package com.example.demo.domain.service;

import com.example.demo.domain.entity.StockIndicator;
import com.example.demo.domain.entity.Top100;
import com.example.demo.domain.repository.StockIndicatorRepository;
import com.example.demo.domain.repository.Top100Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Top100Service {

    private final StockIndicatorRepository stockIndicatorRepository;
    private final Top100Repository top100Repository;

    // ── 진입점 ────────────────────────────────────────────────────────────────

    /**
     * STOCK_INDICATOR 전체 조회 → 스코어 계산 → 상위 100개 TOP100 테이블 저장
     * 같은 날짜 데이터가 이미 존재하면 재계산 없이 건너뜀
     */
    @Transactional
    public void calculateAndSave() {

        LocalDate baseDt = LocalDate.now().minusDays(1);

        if (top100Repository.existsByBaseDt(baseDt)) {
            log.info("[Top100Service] {}일자 데이터가 이미 존재합니다. 건너뜁니다.", baseDt);
            return;
        }

        // PER·PBR·ROE 모두 존재하는 종목만 스코어링 대상 (JOIN FETCH로 N+1 방지)
        List<StockIndicator> indicators = stockIndicatorRepository.findAllWithCompanyForScoring();

        if (indicators.isEmpty()) {
            log.warn("[Top100Service] 스코어링 가능한 지표 데이터가 없습니다.");
            return;
        }

        log.info("[Top100Service] 스코어링 대상 종목 수: {}", indicators.size());

        List<ScoredIndicator> scored = scoreAll(indicators);

        // 점수 내림차순 정렬 후 상위 100개 추출
        scored.sort(Comparator.comparingInt(ScoredIndicator::score).reversed());
        List<ScoredIndicator> top100 = scored.stream().limit(100).collect(Collectors.toList());

        List<Top100> entities = top100.stream()
                .map(s -> Top100.builder()
                        .baseDt(baseDt)
                        .stockCode(s.indicator().getStockCode())
                        .corpCode(s.indicator().getCompany().getCorpCode())
                        .score(s.score())
                        .build())
                .collect(Collectors.toList());

        top100Repository.saveAll(entities);
        log.info("[Top100Service] TOP100 저장 완료: {}건 (baseDt={})", entities.size(), baseDt);
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /** 가장 최근 날짜 기준 점수 상위 10개 반환 */
    public List<Object> getTop10() {
        return top100Repository.findTop10OrderByScoreDesc();
    }

    /** 가장 최근 날짜 기준 전체 100개 슬라이스 페이징 */
    public Slice<Object> getTop100(int page) {
        return top100Repository.findTop100BySlice(PageRequest.of(page, 100));
    }

    // ── 스코어 계산 ───────────────────────────────────────────────────────────

    /**
     * 각 지표를 0~25점(정수)으로 백분위 정규화한 뒤 합산 (총 100점 만점)
     * - PER 낮을수록 고점수 (저평가 매력)
     * - PBR 낮을수록 고점수 (순자산 대비 저평가)
     * - ROE 높을수록 고점수 (수익성)
     * - 배당수익률 높을수록 고점수 (배당매력) — 없으면 0점
     */
    private List<ScoredIndicator> scoreAll(List<StockIndicator> indicators) {

        int n = indicators.size();

        // 각 지표값 추출: 낮을수록 좋은 지표 null → MAX_VALUE, 높을수록 좋은 지표 null → -MAX_VALUE
        double[] pers       = indicators.stream().mapToDouble(i -> { Double v = i.getPer();       return v != null ? v : Double.MAX_VALUE;  }).toArray();
        double[] pbrs       = indicators.stream().mapToDouble(i -> { Double v = i.getPbr();       return v != null ? v : Double.MAX_VALUE;  }).toArray();
        double[] roes       = indicators.stream().mapToDouble(i -> { Double v = i.getRoe();       return v != null ? v : -Double.MAX_VALUE; }).toArray();
        double[] debtRatios = indicators.stream().mapToDouble(i -> { Double v = i.getDebtRatio(); return v != null ? v : Double.MAX_VALUE;  }).toArray();

        // 순위 배열 (0-based): 낮을수록 유리 → ascRank, 높을수록 유리 → descRank
        int[] perRank       = ascRank(pers);
        int[] pbrRank       = ascRank(pbrs);
        int[] roeRank       = descRank(roes);
        int[] debtRatioRank = ascRank(debtRatios);

        List<ScoredIndicator> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // 4개 지표 × 최대 25점 = 100점 만점
            int perScore       = percentileScore(perRank[i], n);
            int pbrScore       = percentileScore(pbrRank[i], n);
            int roeScore       = percentileScore(roeRank[i], n);
            int debtRatioScore = percentileScore(debtRatioRank[i], n);

            result.add(new ScoredIndicator(indicators.get(i), perScore + pbrScore + roeScore + debtRatioScore));
        }
        return result;
    }

    /**
     * 오름차순 순위 배열 반환 (가장 작은 값 → rank 0)
     * PER·PBR처럼 낮을수록 유리한 지표에 사용
     */
    private int[] ascRank(double[] values) {
        int n = values.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        java.util.Arrays.sort(idx, Comparator.comparingDouble(i -> values[i]));

        int[] rank = new int[n];
        for (int r = 0; r < n; r++) rank[idx[r]] = r;
        return rank;
    }

    /**
     * 내림차순 순위 배열 반환 (가장 큰 값 → rank 0)
     * ROE·배당수익률처럼 높을수록 유리한 지표에 사용
     */
    private int[] descRank(double[] values) {
        int n = values.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        idx = java.util.Arrays.stream(idx)
                .sorted((a, b) -> Double.compare(values[b], values[a]))
                .toArray(Integer[]::new);

        int[] rank = new int[n];
        for (int r = 0; r < n; r++) rank[idx[r]] = r;
        return rank;
    }

    /**
     * rank(0-based) → 0~25 점수
     * rank=0(최고) → 25점, rank=n-1(최저) → 0점
     */
    private int percentileScore(int rank, int n) {
        if (n <= 1) return 25;
        return (int) Math.round((double) (n - 1 - rank) / (n - 1) * 25);
    }

    // ── 내부 레코드 ───────────────────────────────────────────────────────────

    private record ScoredIndicator(StockIndicator indicator, int score) {}
}