package com.example.demo.domain.controller;

import com.example.demo.domain.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/{stockCode}")
    public Map<String, Object> getStockDetail(@PathVariable String stockCode) {
        return stockService.getStockDetail(stockCode);
    }
}
