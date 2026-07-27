package com.example.demo.domain.controller;

import com.example.demo.domain.dto.NewsDto;
import com.example.demo.domain.repository.CompanyRepository;
import com.example.demo.domain.repository.StockPriceRepository;
import com.example.demo.domain.service.NewsService;
import com.example.demo.domain.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.*;
import java.util.stream.Collectors;



@Tag(name = "종목", description = "종목 검색, 상세 조회, 관련 뉴스 조회 API")
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final NewsService newsService;
    private final CompanyRepository companyRepository;
    private final StockPriceRepository stockPriceRepository;

    @Operation(summary = "기업/종목 검색", description = "기업명 또는 종목코드(q)로 종목을 검색하여 상위 20건을 반환한다.")
    @GetMapping("/search")
    public List<Map<String, Object>> searchCompanies(
            @Parameter(description = "기업명 또는 종목코드 검색어", example = "삼성전자") @RequestParam String q) {
        return companyRepository
                .findTop20ByCorpNameContainingOrStockCodeContainingOrderByCorpNameAsc(q, q)
                .stream()
                .map(c -> {
                    Long price = stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(c.getStockCode())
                            .map(sp -> sp.getClpr())
                            .orElse(null);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("stockCode", c.getStockCode());
                    item.put("corpName", c.getCorpName());
                    item.put("currentPrice", price);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Operation(summary = "종목 상세 조회", description = "지정한 종목코드(stockCode)의 상세 정보를 조회한다.")
    @GetMapping("/{stockCode}")
    public Map<String, Object> getStockDetail(
            @Parameter(description = "종목코드", example = "005930") @PathVariable String stockCode) {
        return stockService.getStockDetail(stockCode);
    }

    @Operation(summary = "종목 관련 뉴스 조회", description = "지정한 종목코드(stockCode)와 관련된 뉴스를 페이징 조회한다.")
    @GetMapping("/{stockCode}/news")
    public Page<NewsDto> getStockNews(
            @Parameter(description = "종목코드", example = "005930") @PathVariable String stockCode,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page) {
        return newsService.getNews(stockCode, page);
    }
}
