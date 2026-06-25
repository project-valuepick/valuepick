package com.example.demo.domain.scheduled;

import com.example.demo.domain.service.DividendCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DividendScheduler {

    private final DividendCollector dividendCollector;

    // 사업보고서 기준 연 1회 - 4월 1일 새벽 2시 (재무제표 수집 완료 후)
    @Scheduled(cron = "0 0 2 1 4 *")
    public void collectDividend() {
        String year = String.valueOf(LocalDate.now().getYear() - 1);
        log.info("[DividendScheduler] 배당금 수집 시작 - year={}", year);
        dividendCollector.collect(year, "11011");
    }
}