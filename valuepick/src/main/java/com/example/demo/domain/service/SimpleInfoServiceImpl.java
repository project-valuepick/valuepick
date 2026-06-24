package com.example.demo.domain.service;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.repository.StockIndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleInfoServiceImpl implements SimpleInfoService {
    @Autowired
    StockIndicatorRepository indicatorRepository;

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
    public List<Map<String, Object>> getROE() throws Exception {
        return List.of();
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
    public List<Map<String, Object>> getDividendYield() throws Exception {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getTOP10() throws Exception {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getTOP100() throws Exception {
        return List.of();
    }

    @Override
    public Map<String, Object> getList() throws Exception {
        return Map.of();
    }

    @Override
    public Map<String, Object> getListWithFilter(Double perMin, Double perMax, Double roeMin, Double roeMax, Double pbrMin, Double pbrMax, Double dyMin, Double dyMax) throws Exception {
        return Map.of();
    }

    @Override
    public Map<String, Object> getSerachResult(String keyword) throws Exception {
        return Map.of();
    }

    @Override
    public MarketIndexDto getKOSPI() throws Exception {
        return null;
    }

    @Override
    public ExchangeDto getExchange() throws Exception {
        return null;
    }
}
