package com.example.demo.domain.scheduled;

import com.example.demo.domain.repository.MarketIndexRepository;
import com.example.demo.domain.service.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketIndexScheduler {

    private final MarketIndexService marketIndexService;
    private final MarketIndexRepository marketIndexRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    
    @Scheduled(cron = "0 30 8 * * MON-FRI", zone = "Asia/Seoul")
    public void collectMarketIndex() {
        try {
            LocalDate targetDate = LocalDate.now().minusDays(
                    LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY ? 3 : 1);

            if (marketIndexRepository.existsByBasDd(targetDate)) {
                log.info("[MarketIndexScheduler] 코스피 지수 이미 수집됨 - date={}, 스킵", targetDate);
                return;
            }

            log.info("[MarketIndexScheduler] 코스피 지수 수집 시작 - date={}", targetDate);
            marketIndexService.fetchAndSave(targetDate.format(DATE_FORMAT));
            log.info("[MarketIndexScheduler] 코스피 지수 수집 완료");
        } catch (Exception e) {
            log.error("[MarketIndexScheduler] 코스피 지수 수집 실패", e);
        }
    }

    // 7일 이전 코스피 지수 데이터 새벽 2시 30분에 삭제
    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Seoul")
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
