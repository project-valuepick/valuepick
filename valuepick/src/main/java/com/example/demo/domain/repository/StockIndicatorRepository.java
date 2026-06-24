package com.example.demo.domain.repository;

import com.example.demo.domain.entity.StockIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// StockIndicator의 PK는 stock_code(String) - 기존 FinancialIndicator(Long PK)에서 변경됨
@Repository
public interface StockIndicatorRepository extends JpaRepository<StockIndicator, String> {
}
