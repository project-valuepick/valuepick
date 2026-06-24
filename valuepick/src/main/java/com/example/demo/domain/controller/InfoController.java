package com.example.demo.domain.controller;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.service.SimpleInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/info")
public class InfoController {
    @Autowired
    SimpleInfoService simpleInfoService;

    // 저PER 상위 5
    @GetMapping(value = "/per",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getPER() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPER());
    }
    // 저PBR 상위 5
    @GetMapping(value = "/pbr",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getPBR() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPBR());
    }

    // 고ROE 상위 5
    @GetMapping(value = "/roe",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getROE() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getROE());
    }

    // 고배당수익률 상위 5
    @GetMapping(value = "/dividend-yield",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getDividendYield() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getDividendYield());
    }

    // 코스피
    @GetMapping(value = "/kospi",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MarketIndexDto> getKOSPI() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getKOSPI());
    }

    // 환율
    @GetMapping(value = "/exchange",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExchangeDto> getExchange() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getExchange());
    }
}
