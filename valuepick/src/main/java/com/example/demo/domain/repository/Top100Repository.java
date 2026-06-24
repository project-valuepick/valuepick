package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Top100;
import com.example.demo.domain.entity.Top100Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface Top100Repository extends JpaRepository<Top100, Top100Id> {

    @Query(value = """
            SELECT t.stock_code, t.score, c.corp_name
            FROM TOP100 t
            JOIN COMPANY c ON t.stock_code = c.stock_code
            ORDER BY t.score DESC 
            LIMIT 10
            """,
            nativeQuery = true)
    List<Object> findTop10OrderByScoreDesc();
    @Query(value = """
            SELECT t.stock_code, t.score, c.corp_name
            FROM TOP100 t
            JOIN COMPANY c ON t.stock_code = c.stock_code
            ORDER BY t.score DESC 
            """,
            nativeQuery = true)
    List<Object> findTop100OrderByScoreDesc();
}
