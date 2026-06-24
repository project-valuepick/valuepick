package com.example.demo.domain.repository;

import com.example.demo.domain.entity.StockIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockIndicatorRepository extends JpaRepository<StockIndicator, String> {
    //지표 테이블 들어오는 것 보고 다시 수정요망
    @Query(value = """
            SELECT i.stock_code, i.per, c.corp_name
            FROM STOCK_INDICATOR i
            JOIN COMPANY c ON i.stock_code = c.stock_code
            ORDER BY i.per ASC
            LIMIT 5
            """,
            nativeQuery = true)
    List<Object> lowerPer5();

    @Query(value = """
            SELECT i.stock_code, i.pbr, c.corp_name
            FROM STOCK_INDICATOR i
            JOIN COMPANY c ON i.stock_code = c.stock_code
            ORDER BY i.pbr ASC
            LIMIT 5
            """,
            nativeQuery = true)
    List<Object> lowerPbr5();

    @Query(value = """
            SELECT i.stock_code, i.roe, c.corp_name
            FROM STOCK_INDICATOR i
            JOIN COMPANY c ON i.stock_code = c.stock_code
            ORDER BY i.roe DESC 
            LIMIT 5
            """,
            nativeQuery = true)
    List<Object> higherRoe5();
}
