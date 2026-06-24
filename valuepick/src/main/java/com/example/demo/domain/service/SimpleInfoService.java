package com.example.demo.domain.service;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;

import java.util.List;
import java.util.Map;

public interface SimpleInfoService {
    //저PER
    List<Map<String,Object>> getPER() throws Exception;
    //저PBR
    List<Map<String,Object>> getPBR() throws Exception;
    //고ROE
    List<Map<String,Object>> getROE() throws Exception;
    //배당수익률
    List<Map<String,Object>> getDividendYield() throws Exception;
    //rank top100 중 10개만
    List<Map<String,Object>> getTOP10() throws Exception;
    //rank top 100 전체 조회
    List<Map<String,Object>> getTOP100() throws Exception;
    //list
    Map<String,Object> getList() throws Exception;
    //list with filter (per, roe, pbr, dividendYield 최소/최대)
    Map<String,Object> getListWithFilter(Double perMin, Double perMax,
                                         Double roeMin, Double roeMax,
                                         Double pbrMin, Double pbrMax,
                                         Double dyMin,  Double dyMax) throws Exception;
    //search
    Map<String,Object> getSerachResult(String keyword) throws Exception;
    //코스피
    MarketIndexDto getKOSPI() throws Exception;
    //환율
    ExchangeDto getExchange() throws Exception;
    //코스닥 -- 생략
}
