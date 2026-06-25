package com.example.demo.domain.scheduled;

import com.example.demo.domain.repository.ExchangeRepository;
import com.example.demo.domain.service.ExchangeRateApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeScheduler {

    private final ExchangeRateApiService exchangeRateApiService;
    private final ExchangeRepository exchangeRepository;

    @Scheduled(cron = "0 0 11 * * MON-FRI")
    public void collectExchangeRate() {
        try {
            log.info("[ExchangeScheduler] 환율 수집 시작");
            exchangeRateApiService.fetchAndSaveExchangeRatesForToday();
            log.info("[ExchangeScheduler] 환율 수집 완료");
        } catch (Exception e) {
            log.error("[ExchangeScheduler] 환율 수집 실패", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldExchangeRate() {
        try {
            LocalDate cutoff = LocalDate.now().minusDays(7);
            log.info("[ExchangeScheduler] 7일 이전 환율 삭제 - cutoff={}", cutoff);
            exchangeRepository.deleteByBaseDateBefore(cutoff);
        } catch (Exception e) {
            log.error("[ExchangeScheduler] 7일 이전 환율 삭제 실패", e);
        }
    }
}