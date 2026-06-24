package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    // 전체 종목코드 리스트 조회 (null 제외) - 스케줄러에서 종목 목록 뽑을 때 사용
    @Query("SELECT c.stockCode FROM Company c WHERE c.stockCode IS NOT NULL")
    List<String> findAllStockCodes();

    // 페이징 처리된 Company 전체 조회 - 대용량 데이터를 100건씩 나눠서 처리할 때 사용
    Page<Company> findAll(Pageable pageable);

    // 페이징 처리된 종목코드만 조회 - StockPriceCollector에서 종목코드만 필요할 때 사용
    @Query("SELECT c.stockCode FROM Company c")
    Page<String> findAllStockCodes(Pageable pageable);
}
