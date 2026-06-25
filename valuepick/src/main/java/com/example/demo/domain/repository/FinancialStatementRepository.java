package com.example.demo.domain.repository;

import com.example.demo.domain.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    // 특정 종목 + 특정 연도 재무제표 조회 - 지표 계산 시 해당 연도 데이터만 가져올 때 사용
    // bsnsYear가 String 타입이므로 year도 String으로 받음
    @Query("SELECT f FROM FinancialStatement f WHERE f.company.stockCode = :stockCode AND f.bsnsYear = :year AND f.reprtCode = :reprtCode")
    Optional<FinancialStatement> findByStockCodeAndYearAndReprtCode(String stockCode, String year, String reprtCode);

    List<FinancialStatement> findByCompany_StockCodeOrderByBsnsYearDesc(String stockCode);
}
