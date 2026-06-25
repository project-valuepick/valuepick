package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {

    @Query(value = """
            SELECT c.stock_code, c.corp_name,
                   i.per, i.roe, i.pbr, i.dividend_yield,
                   sp.mkp, sp.flt_rt, sp.mrkt_tot_amt
            FROM COMPANY c
            LEFT JOIN STOCK_INDICATOR i ON c.stock_code = i.stock_code
            LEFT JOIN STOCK_PRICE sp ON c.stock_code = sp.srtn_cd
                AND sp.bas_dt = (
                    SELECT MAX(sp2.bas_dt)
                    FROM STOCK_PRICE sp2
                    WHERE sp2.srtn_cd = c.stock_code
                )
            WHERE c.corp_name LIKE %:keyword%
            """, nativeQuery = true)
    List<Object> searchByCorpName(@Param("keyword") String keyword);

    @Query(value = """
            SELECT c.stock_code, c.corp_name,
                   i.per, i.roe, i.pbr, i.dividend_yield,
                   sp.mkp, sp.flt_rt, sp.mrkt_tot_amt
            FROM COMPANY c
            LEFT JOIN STOCK_INDICATOR i ON c.stock_code = i.stock_code
            LEFT JOIN STOCK_PRICE sp ON c.stock_code = sp.srtn_cd
                AND sp.bas_dt = (
                    SELECT MAX(sp2.bas_dt)
                    FROM STOCK_PRICE sp2
                    WHERE sp2.srtn_cd = c.stock_code
                )
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM COMPANY c
            LEFT JOIN STOCK_INDICATOR i ON c.stock_code = i.stock_code
            LEFT JOIN STOCK_PRICE sp ON c.stock_code = sp.srtn_cd
                AND sp.bas_dt = (
                    SELECT MAX(sp2.bas_dt)
                    FROM STOCK_PRICE sp2
                    WHERE sp2.srtn_cd = c.stock_code
                )
            """,
            nativeQuery = true)
    Page<Object> findAllWithIndicatorAndPrice(Pageable pageable);

    @Query(value = """
            SELECT c.stock_code, c.corp_name,
                   i.per, i.roe, i.pbr, i.dividend_yield,
                   sp.mkp, sp.flt_rt, sp.mrkt_tot_amt
            FROM COMPANY c
            LEFT JOIN STOCK_INDICATOR i ON c.stock_code = i.stock_code
            LEFT JOIN STOCK_PRICE sp ON c.stock_code = sp.srtn_cd
                AND sp.bas_dt = (
                    SELECT MAX(sp2.bas_dt)
                    FROM STOCK_PRICE sp2
                    WHERE sp2.srtn_cd = c.stock_code
                )
            WHERE (:perMin IS NULL OR i.per >= :perMin)
              AND (:perMax IS NULL OR i.per <= :perMax)
              AND (:roeMin IS NULL OR i.roe >= :roeMin)
              AND (:roeMax IS NULL OR i.roe <= :roeMax)
              AND (:pbrMin IS NULL OR i.pbr >= :pbrMin)
              AND (:pbrMax IS NULL OR i.pbr <= :pbrMax)
              AND (:dyMin  IS NULL OR i.dividend_yield >= :dyMin)
              AND (:dyMax  IS NULL OR i.dividend_yield <= :dyMax)
            """, nativeQuery = true)
    List<Object> findAllWithIndicatorAndPriceFiltered(
            @Param("perMin") Double perMin, @Param("perMax") Double perMax,
            @Param("roeMin") Double roeMin, @Param("roeMax") Double roeMax,
            @Param("pbrMin") Double pbrMin, @Param("pbrMax") Double pbrMax,
            @Param("dyMin")  Double dyMin,  @Param("dyMax")  Double dyMax);
}
