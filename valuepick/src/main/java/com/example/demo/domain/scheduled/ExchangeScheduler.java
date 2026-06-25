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

    // 평일 오전 11시 (한국수출입은행 API 업데이트 시간 기준)
    @Scheduled(cron = "0 0 11 * * MON-FRI")
    public void collectExchangeRate() {
        log.info("[ExchangeScheduler] 환율 수집 시작");
        exchangeRateApiService.fetchAndSaveExchangeRatesForToday();
        log.info("[ExchangeScheduler] 환율 수집 완료");
    }

    // 매일 새벽 2시 - 7일 이전 데이터 삭제
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldExchangeRate() {
        LocalDate cutoff = LocalDate.now().minusDays(7);
        log.info("[ExchangeScheduler] 7일 이전 환율 삭제 - cutoff={}", cutoff);
        exchangeRepository.deleteByBaseDateBefore(cutoff);
    }
}