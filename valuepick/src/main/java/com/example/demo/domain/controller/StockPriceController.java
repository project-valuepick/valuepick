package com.example.demo.domain.controller;

import com.example.demo.domain.service.StockPriceCollector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Tag(name = "주가 수집 (관리자)", description = "종목별 주가 데이터를 외부 API에서 수집하여 저장하는 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/stock")
public class StockPriceController {

    private final StockPriceCollector stockPriceCollector;

    @Operation(summary = "전체 종목 주가 수집", description = "날짜 하나만 입력하면 단일 날짜, 두 개 입력하면 기간(startDate~endDate)으로 전체 종목 주가를 수집한다. 비동기로 즉시 응답 후 백그라운드에서 수집을 진행한다.")
    @GetMapping({"/collect/{startDate}", "/collect/{startDate}/{endDate}"})
    public ResponseEntity<Map<String, String>> collect(
            @PathVariable String startDate,
            @PathVariable(required = false) String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = (endDate == null) ? start : LocalDate.parse(endDate, formatter);

        stockPriceCollector.collect(start, end); // 비동기 실행 - 바로 응답 반환

        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "startDate", start.toString(),
                "endDate", end.toString()
        ));
    }
}
