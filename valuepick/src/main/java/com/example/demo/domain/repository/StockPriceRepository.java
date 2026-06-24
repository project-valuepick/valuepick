package com.example.demo.domain.repository;

import com.example.demo.domain.entity.StockPrice;
import com.example.demo.domain.entity.StockPriceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {

    List<StockPrice> findBySrtnCdAndBasDtGreaterThanEqualOrderByBasDtAsc(String srtnCd, LocalDate basDt);
}
