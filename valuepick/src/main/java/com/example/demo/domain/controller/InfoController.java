package com.example.demo.domain.controller;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.service.SimpleInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "종합 정보", description = "PER/PBR/ROE/배당수익률 상위 종목, 코스피, 환율, TOP10/TOP100, 종목 목록 검색/필터 조회 API")
@RestController
@RequestMapping("/info")
public class InfoController {
    @Autowired
    SimpleInfoService simpleInfoService;

    @Operation(summary = "저PER 상위 5 조회", description = "PER(주가수익비율)이 낮은 상위 5개 종목을 조회한다.")
    @GetMapping(value = "/per",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getPER() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPER());
    }

    @Operation(summary = "저PBR 상위 5 조회", description = "PBR(주가순자산비율)이 낮은 상위 5개 종목을 조회한다.")
    @GetMapping(value = "/pbr",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getPBR() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPBR());
    }

    @Operation(summary = "고ROE 상위 5 조회", description = "ROE(자기자본이익률)가 높은 상위 5개 종목을 조회한다.")
    @GetMapping(value = "/roe",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getROE() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getROE());
    }

    @Operation(summary = "고배당수익률 상위 5 조회", description = "배당수익률이 높은 상위 5개 종목을 조회한다.")
    @GetMapping(value = "/dividend-yield",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getDividendYield() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getDividendYield());
    }

    @Operation(summary = "코스피 지수 조회", description = "최신 코스피 지수를 조회한다.")
    @GetMapping(value = "/kospi",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MarketIndexDto> getKOSPI() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getKOSPI());
    }

    @Operation(summary = "환율 조회", description = "최신 환율 정보를 조회한다.")
    @GetMapping(value = "/exchange",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> getExchange() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getExchange());
    }

    @Operation(summary = "TOP10 조회", description = "가치투자 스코어 상위 10개 종목을 조회한다.")
    @GetMapping(value = "/top10",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTOP10() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getTOP10());
    }

    @Operation(summary = "TOP100 슬라이스 조회", description = "가치투자 스코어 상위 100개 종목을 페이지/사이즈 기준으로 무한스크롤 방식으로 조회한다.")
    @GetMapping(value = "/top100",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTOP100(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getTOP100(page, size));
    }

    @Operation(summary = "전체 종목 목록 조회", description = "기업 정보, 투자지표, 최신 주가를 결합한 전체 종목 목록을 페이징/정렬하여 조회한다.")
    @GetMapping(value = "/list",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "per") @RequestParam(required = false)    String sort,
            @Parameter(description = "정렬 방향 (asc/desc)", example = "asc") @RequestParam(required = false)    String dir) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getList(page, size, sort, dir));
    }

    @Operation(summary = "필터 조건별 종목 목록 조회", description = "PER, ROE, PBR, 배당수익률의 최소/최대 범위로 필터링하여 종목 목록을 페이징 조회한다.")
    @GetMapping(value = "/list/filter",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getListWithFilter(
            @Parameter(description = "PER 최소값") @RequestParam(required = false) Double perMin,
            @Parameter(description = "PER 최대값") @RequestParam(required = false) Double perMax,
            @Parameter(description = "ROE 최소값") @RequestParam(required = false) Double roeMin,
            @Parameter(description = "ROE 최대값") @RequestParam(required = false) Double roeMax,
            @Parameter(description = "PBR 최소값") @RequestParam(required = false) Double pbrMin,
            @Parameter(description = "PBR 최대값") @RequestParam(required = false) Double pbrMax,
            @Parameter(description = "배당수익률 최소값") @RequestParam(required = false) Double dyMin,
            @Parameter(description = "배당수익률 최대값") @RequestParam(required = false) Double dyMax,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "per") @RequestParam(required = false)    String sort,
            @Parameter(description = "정렬 방향 (asc/desc)", example = "asc") @RequestParam(required = false)    String dir) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getListWithFilter(
                perMin, perMax, roeMin, roeMax, pbrMin, pbrMax, dyMin, dyMax, page, size, sort, dir));
    }

    @Operation(summary = "기업명 검색", description = "키워드(keyword)로 기업명을 검색하여 결과를 페이징 조회한다.")
    @GetMapping(value = "/search",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(
            @Parameter(description = "검색할 기업명 키워드", example = "삼성") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "per") @RequestParam(required = false)    String sort,
            @Parameter(description = "정렬 방향 (asc/desc)", example = "asc") @RequestParam(required = false)    String dir) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getSerachResult(keyword, page, size, sort, dir));
    }

}
