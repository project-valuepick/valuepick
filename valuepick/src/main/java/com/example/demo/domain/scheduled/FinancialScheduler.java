package com.example.demo.domain.scheduled;

import com.example.demo.domain.service.DartFinancialCollector;
import com.example.demo.domain.service.FinancialIndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialScheduler {

    private final DartFinancialCollector dartFinancialCollector;
    private final FinancialIndicatorService financialIndicatorService;

    // 사업보고서 (11011) - 4월 1일 새벽 1시 수집, 3시 지표계산
    @Scheduled(cron = "0 0 1 1 4 *")
    public void collectAnnual() {
        String year = String.valueOf(LocalDate.now().getYear() - 1);
        log.info("[FinancialScheduler] 사업보고서 수집 시작 - year={}", year);
        dartFinancialCollector.collect(year, "11011");
    }

    @Scheduled(cron = "0 0 3 1 4 *")
    public void calculateAnnual() {
        String year = String.valueOf(LocalDate.now().getYear() - 1);
        log.info("[FinancialScheduler] 사업보고서 지표계산 시작 - year={}", year);
        financialIndicatorService.calculateAll(Integer.parseInt(year), "11011");
    }

    // 1분기보고서 (11013) - 6월 1일
    @Scheduled(cron = "0 0 1 1 6 *")
    public void collectQ1() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 1분기보고서 수집 시작 - year={}", year);
        dartFinancialCollector.collect(year, "11013");
    }

    @Scheduled(cron = "0 0 3 1 6 *")
    public void calculateQ1() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 1분기보고서 지표계산 시작 - year={}", year);
        financialIndicatorService.calculateAll(Integer.parseInt(year), "11013");
    }

    // 반기보고서 (11012) - 9월 1일
    @Scheduled(cron = "0 0 1 1 9 *")
    public void collectHalf() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 반기보고서 수집 시작 - year={}", year);
        dartFinancialCollector.collect(year, "11012");
    }

    @Scheduled(cron = "0 0 3 1 9 *")
    public void calculateHalf() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 반기보고서 지표계산 시작 - year={}", year);
        financialIndicatorService.calculateAll(Integer.parseInt(year), "11012");
    }

    // 3분기보고서 (11014) - 12월 1일
    @Scheduled(cron = "0 0 1 1 12 *")
    public void collectQ3() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 3분기보고서 수집 시작 - year={}", year);
        dartFinancialCollector.collect(year, "11014");
    }

    @Scheduled(cron = "0 0 3 1 12 *")
    public void calculateQ3() {
        String year = String.valueOf(LocalDate.now().getYear());
        log.info("[FinancialScheduler] 3분기보고서 지표계산 시작 - year={}", year);
        financialIndicatorService.calculateAll(Integer.parseInt(year), "11014");
    }
}