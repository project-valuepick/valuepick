package com.example.demo.domain.controller;

import com.example.demo.domain.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class CollectorTest {

    @Autowired
    private DartCompanyCollector dartCompanyCollector;
    @Autowired
    private DartFinancialCollector dartFinancialCollector;
    @Autowired
    private DividendCollector dividendCollector;
    @Autowired
    private StockPriceCollector stockPriceCollector;
    @Autowired
    private ExchangeRateApiService exchangeRateApiService;
    @Autowired
    private MarketIndexService marketIndexService;
    @Autowired
    private FinancialIndicatorService financialIndicatorService;
    @Autowired
    private Top100Service top100Service;



    @Test
    public void t1_기업정보수집() {
        dartCompanyCollector.collectCompanies("20260623");
    }

    @Test
    public void t2_재무제표수집() {
        dartFinancialCollector.collect("2025", "11011");
    }

    @Test
    public void t3_배당금수집() {
        dividendCollector.collect("2025", "11011");
    }

    @Test
    public void t4_주가수집() {
        LocalDate date = LocalDate.of(2026, 6,23);
        stockPriceCollector.collect(date, date);
    }

    @Test
    public void t5_환율수집() {
        exchangeRateApiService.fetchAndSaveExchangeRates("20260623");
    }

    @Test
    public void t6_코스피지수수집() {
        marketIndexService.fetchAndSave("20260623");
    }

    @Test
    public void t7_지표계산() {
        financialIndicatorService.calculateAll("2025", "11011");
    }

    @Test
    public void t8_Top100스코어계산() {
        top100Service.calculateAndSave();
    }
}
