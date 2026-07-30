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

    // 월-금 08:30 수집 - KRX 지수는 08:00경 확정되므로 여유를 두고 조회 (전 영업일자, 월요일은 금요일자)
    @Scheduled(cron = "0 30 8 * * MON-FRI", zone = "Asia/Seoul")
    public void collectMarketIndex() {
        try {
            LocalDate targetDate = LocalDate.now().minusDays(
                    LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY ? 3 : 1);

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
