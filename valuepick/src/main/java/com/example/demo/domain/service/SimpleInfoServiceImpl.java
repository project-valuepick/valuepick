package com.example.demo.domain.service;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.entity.Exchange;
import com.example.demo.domain.entity.MarketIndex;
import com.example.demo.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleInfoServiceImpl implements SimpleInfoService {
    @Autowired
    StockIndicatorRepository indicatorRepository;

    @Autowired
    MarketIndexRepository marketIndexRepository;

    @Autowired
    ExchangeRepository exchangeRepository;

    @Autowired
    Top100Repository top100Repository;

    @Autowired
    CompanyRepository companyRepository;

    @Override
    public List<Map<String,Object>> getPER() throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Object> objects = indicatorRepository.lowerPer5();
        for(Object o : objects){
            Object[] row = (Object[]) o;
            Map<String,Object> m = new HashMap<>();
            m.put("stock_code",row[0]);
            m.put("per",row[1]);
            m.put("corp_name",row[2]);
            list.add(m);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> getPBR() throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Object> objects = indicatorRepository.lowerPbr5();
        for(Object o : objects){
            Object[] row = (Object[]) o;
            Map<String,Object> m = new HashMap<>();
            m.put("stock_code",row[0]);
            m.put("pbr",row[1]);
            m.put("corp_name",row[2]);
            list.add(m);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> getROE() throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Object> objects = indicatorRepository.higherRoe5();
        for(Object o : objects){
            Object[] row = (Object[]) o;
            Map<String,Object> m = new HashMap<>();
            m.put("stock_code",row[0]);
            m.put("roe",row[1]);
            m.put("corp_name",row[2]);
            list.add(m);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> getDividendYield() throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Object> objects = indicatorRepository.higherDY5();
        for(Object o : objects){
            Object[] row = (Object[]) o;
            Map<String,Object> m = new HashMap<>();
            m.put("stock_code",row[0]);
            m.put("dividend_yield",row[1]);
            m.put("corp_name",row[2]);
            list.add(m);
        }

        return list;
    }

    @Override
    public Map<String, Object> getTOP10() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object> objects = top100Repository.findTop10OrderByScoreDesc();
        for (Object o : objects) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code",     row[0]);
            m.put("corp_name",      row[1]);
            m.put("per",            row[2]);
            m.put("roe",            row[3]);
            m.put("pbr",            row[4]);
            m.put("dividend_yield", row[5]);
            m.put("mkp",            row[6]);
            m.put("flt_rt",         row[7]);
            m.put("mrkt_tot_amt",   row[8]);
            m.put("score",          row[9]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }

    @Override
    public Map<String, Object> getTOP100() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object> objects = top100Repository.findTop100OrderByScoreDesc();
        for (Object o : objects) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code",     row[0]);
            m.put("corp_name",      row[1]);
            m.put("per",            row[2]);
            m.put("roe",            row[3]);
            m.put("pbr",            row[4]);
            m.put("dividend_yield", row[5]);
            m.put("mkp",            row[6]);
            m.put("flt_rt",         row[7]);
            m.put("mrkt_tot_amt",   row[8]);
            m.put("score",          row[9]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }

    @Override
    public MarketIndexDto getKOSPI() throws Exception {
        MarketIndex kospi = marketIndexRepository
                .findTop1ByIdxNmOrderByBasDdDesc("KOSPI")
                .orElseThrow(() -> new Exception("KOSPI 데이터가 없습니다."));
        return MarketIndexDto.from(kospi);
    }

    @Override
    public ExchangeDto getExchange() throws Exception {
        Exchange exchange = exchangeRepository
                .findById("USD")
                .orElseThrow(() -> new Exception("환율 데이터가 없습니다."));
        return ExchangeDto.from(exchange);
    }

    @Override
    public Map<String, Object> getList(int page, int size) throws Exception {
        Page<Object> pageResult = companyRepository.findAllWithIndicatorAndPrice(PageRequest.of(page, size));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : pageResult.getContent()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code",     row[0]);
            m.put("corp_name",      row[1]);
            m.put("per",            row[2]);
            m.put("roe",            row[3]);
            m.put("pbr",            row[4]);
            m.put("dividend_yield", row[5]);
            m.put("mkp",            row[6]);
            m.put("flt_rt",         row[7]);
            m.put("mrkt_tot_amt",   row[8]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list",       list);
        result.put("totalCount", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("page",       page);
        return result;
    }


    @Override
    public Map<String, Object> getListWithFilter(Double perMin, Double perMax,
                                                 Double roeMin, Double roeMax,
                                                 Double pbrMin, Double pbrMax,
                                                 Double dyMin,  Double dyMax,
                                                 int page, int size) throws Exception {
        Page<Object> pageResult = companyRepository.findAllWithIndicatorAndPriceFiltered(
                perMin, perMax, roeMin, roeMax, pbrMin, pbrMax, dyMin, dyMax,
                PageRequest.of(page, size));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : pageResult.getContent()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code",     row[0]);
            m.put("corp_name",      row[1]);
            m.put("per",            row[2]);
            m.put("roe",            row[3]);
            m.put("pbr",            row[4]);
            m.put("dividend_yield", row[5]);
            m.put("mkp",            row[6]);
            m.put("flt_rt",         row[7]);
            m.put("mrkt_tot_amt",   row[8]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list",       list);
        result.put("totalCount", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("page",       page);
        return result;
    }

    @Override
    public Map<String, Object> getSerachResult(String keyword, int page, int size) throws Exception {
        Page<Object> pageResult = companyRepository.searchByCorpName(keyword, PageRequest.of(page, size));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : pageResult.getContent()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code",     row[0]);
            m.put("corp_name",      row[1]);
            m.put("per",            row[2]);
            m.put("roe",            row[3]);
            m.put("pbr",            row[4]);
            m.put("dividend_yield", row[5]);
            m.put("mkp",            row[6]);
            m.put("flt_rt",         row[7]);
            m.put("mrkt_tot_amt",   row[8]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result",     list);
        result.put("totalCount", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("page",       page);
        return result;
    }

}
