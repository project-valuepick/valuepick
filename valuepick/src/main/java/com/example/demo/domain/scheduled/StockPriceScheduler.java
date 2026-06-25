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

    // 평일 장 마감 후 - 오후 4시
    @Scheduled(cron = "0 0 16 * * MON-FRI")
    public void collectStockPrice() {
        LocalDate today = LocalDate.now();
        log.info("[StockPriceScheduler] 주가 수집 시작 - date={}", today);
        stockPriceCollector.collect(today, today);
    }

    // 매일 새벽 2시 - 7일 이전 데이터 삭제
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldStockPrice() {
        LocalDate cutoff = LocalDate.now().minusDays(7);
        log.info("[StockPriceScheduler] 7일 이전 주가 삭제 - cutoff={}", cutoff);
        stockPriceRepository.deleteByBasDtBefore(cutoff);
    }
}