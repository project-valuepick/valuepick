package com.example.demo.domain.scheduled;

import com.example.demo.domain.repository.MarketIndexRepository;
import com.example.demo.domain.service.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketIndexScheduler {

    private final MarketIndexService marketIndexService;
    private final MarketIndexRepository marketIndexRepository;

    @Scheduled(cron = "0 0 16 * * MON-FRI")
    public void collectMarketIndex() {
        try {
            log.info("[MarketIndexScheduler] 코스피 지수 수집 시작");
            marketIndexService.fetchAndSaveForToday();
            log.info("[MarketIndexScheduler] 코스피 지수 수집 완료");
        } catch (Exception e) {
            log.error("[MarketIndexScheduler] 코스피 지수 수집 실패", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldMarketIndex() {
        try {
            LocalDate cutoff = LocalDate.now().minusDays(7);
            log.info("[MarketIndexScheduler] 7일 이전 코스피 지수 삭제 - cutoff={}", cutoff);
            marketIndexRepository.deleteByBasDdBefore(cutoff);
        } catch (Exception e) {
            log.error("[MarketIndexScheduler] 7일 이전 코스피 지수 삭제 실패", e);
        }
    }
}
