package com.example.demo.domain.scheduled;

import com.example.demo.domain.repository.StockPriceRepository;
import com.example.demo.domain.service.StockPriceCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceScheduler {

    private final StockPriceCollector stockPriceCollector;
    private final StockPriceRepository stockPriceRepository;

    @Scheduled(cron = "0 0 16 * * MON-FRI")
    public void collectStockPrice() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1); // 전일 기준
            log.info("[StockPriceScheduler] 주가 수집 시작 - date={}", yesterday);
            stockPriceCollector.collect(yesterday, yesterday);
        } catch (Exception e) {
            log.error("[StockPriceScheduler] 주가 수집 실패", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldStockPrice() {
        try {
            LocalDate cutoff = LocalDate.now().minusDays(7);
            log.info("[StockPriceScheduler] 7일 이전 주가 삭제 - cutoff={}", cutoff);
            stockPriceRepository.deleteByBasDtBefore(cutoff);
        } catch (Exception e) {
            log.error("[StockPriceScheduler] 7일 이전 주가 삭제 실패", e);
        }
    }
}