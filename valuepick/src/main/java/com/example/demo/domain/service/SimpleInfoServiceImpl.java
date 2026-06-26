package com.example.demo.domain.service;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.entity.Exchange;
import com.example.demo.domain.entity.MarketIndex;
import com.example.demo.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleInfoServiceImpl implements SimpleInfoService {

    @Autowired StockIndicatorRepository indicatorRepository;
    @Autowired MarketIndexRepository    marketIndexRepository;
    @Autowired ExchangeRepository       exchangeRepository;
    @Autowired Top100Repository         top100Repository;
    @Autowired CompanyRepository        companyRepository;
    @Autowired EntityManager            entityManager;

    // ── 공통 SQL 베이스 ───────────────────────────────────────────
    private static final String LIST_SELECT = """
            SELECT c.stock_code, c.corp_name,
                   i.per, i.roe, i.pbr, i.dividend_yield,
                   sp.mkp, sp.flt_rt, sp.mrkt_tot_amt
            FROM COMPANY c
            LEFT JOIN STOCK_INDICATOR i  ON c.stock_code = i.stock_code
            LEFT JOIN STOCK_PRICE     sp ON c.stock_code = sp.srtn_cd
                AND sp.bas_dt = (
                    SELECT MAX(sp2.bas_dt) FROM STOCK_PRICE sp2
                    WHERE sp2.srtn_cd = c.stock_code
                )
            """;

    // ── 정렬 헬퍼 ─────────────────────────────────────────────────
    private String toDbColumn(String key) {
        if (key == null || key.isBlank()) return null;
        return switch (key) {
            case "dividendYield" -> "dividend_yield";
            case "price"         -> "mkp";
            case "changeRate"    -> "flt_rt";
            case "marketCap"     -> "mrkt_tot_amt";
            default              -> key;
        };
    }

    private String orderClause(String sort, String dir) {
        String col = toDbColumn(sort);
        if (col == null) return "";
        return " ORDER BY " + col + " " + ("asc".equalsIgnoreCase(dir) ? "ASC" : "DESC");
    }

    // ── 결과 행 → Map ──────────────────────────────────────────────
    private Map<String, Object> rowToMap(Object[] row) {
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
        return m;
    }

    private Map<String, Object> toPageResult(List<?> content, long total, int page, int size) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : content) list.add(rowToMap((Object[]) o));
        Map<String, Object> result = new HashMap<>();
        result.put("list",       list);
        result.put("totalCount", total);
        result.put("totalPages", size > 0 ? (int) Math.ceil((double) total / size) : 0);
        result.put("page",       page);
        return result;
    }

    // ── 랭킹 ──────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getPER() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : indicatorRepository.lowerPer5()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("per", row[1]); m.put("corp_name", row[2]);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getPBR() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : indicatorRepository.lowerPbr5()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("pbr", row[1]); m.put("corp_name", row[2]);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getROE() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : indicatorRepository.higherRoe5()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("roe", row[1]); m.put("corp_name", row[2]);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getDividendYield() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : indicatorRepository.higherDY5()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("dividend_yield", row[1]); m.put("corp_name", row[2]);
            list.add(m);
        }
        return list;
    }

    // ── TOP10 / TOP100 ────────────────────────────────────────────
    @Override
    public Map<String, Object> getTOP10() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : top100Repository.findTop10OrderByScoreDesc()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("corp_name", row[1]);
            m.put("per", row[2]);       m.put("roe", row[3]);
            m.put("pbr", row[4]);       m.put("dividend_yield", row[5]);
            m.put("mkp", row[6]);       m.put("flt_rt", row[7]);
            m.put("mrkt_tot_amt", row[8]); m.put("score", row[9]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }

    @Override
    public Map<String, Object> getTOP100(int page, int size) throws Exception {
        Slice<Object> slice = top100Repository.findTop100BySlice(PageRequest.of(page, size));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : slice.getContent()) {
            Object[] row = (Object[]) o;
            Map<String, Object> m = new HashMap<>();
            m.put("stock_code", row[0]); m.put("corp_name", row[1]);
            m.put("per", row[2]);       m.put("roe", row[3]);
            m.put("pbr", row[4]);       m.put("dividend_yield", row[5]);
            m.put("mkp", row[6]);       m.put("flt_rt", row[7]);
            m.put("mrkt_tot_amt", row[8]); m.put("score", row[9]);
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list",    list);
        result.put("hasNext", slice.hasNext());
        result.put("page",    page);
        return result;
    }

    // ── 시장 지표 ─────────────────────────────────────────────────
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

    // ── 전체 목록 (EntityManager 직접 SQL — Pageable Sort 회피) ───
    @Override
    public Map<String, Object> getList(int page, int size, String sort, String dir) throws Exception {
        String countSql = """
                SELECT COUNT(*) FROM COMPANY c
                LEFT JOIN STOCK_INDICATOR i  ON c.stock_code = i.stock_code
                LEFT JOIN STOCK_PRICE     sp ON c.stock_code = sp.srtn_cd
                    AND sp.bas_dt = (
                        SELECT MAX(sp2.bas_dt) FROM STOCK_PRICE sp2
                        WHERE sp2.srtn_cd = c.stock_code
                    )
                """;
        List<?> rows = entityManager.createNativeQuery(LIST_SELECT + orderClause(sort, dir))
                .setFirstResult(page * size).setMaxResults(size).getResultList();
        long total = ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();
        return toPageResult(rows, total, page, size);
    }

    // ── 필터 목록 ─────────────────────────────────────────────────
    @Override
    public Map<String, Object> getListWithFilter(Double perMin, Double perMax,
                                                  Double roeMin, Double roeMax,
                                                  Double pbrMin, Double pbrMax,
                                                  Double dyMin,  Double dyMax,
                                                  int page, int size, String sort, String dir) throws Exception {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        if (perMin != null) { where.append(" AND i.per >= :perMin"); params.put("perMin", perMin); }
        if (perMax != null) { where.append(" AND i.per <= :perMax"); params.put("perMax", perMax); }
        if (roeMin != null) { where.append(" AND i.roe >= :roeMin"); params.put("roeMin", roeMin); }
        if (roeMax != null) { where.append(" AND i.roe <= :roeMax"); params.put("roeMax", roeMax); }
        if (pbrMin != null) { where.append(" AND i.pbr >= :pbrMin"); params.put("pbrMin", pbrMin); }
        if (pbrMax != null) { where.append(" AND i.pbr <= :pbrMax"); params.put("pbrMax", pbrMax); }
        if (dyMin  != null) { where.append(" AND i.dividend_yield >= :dyMin"); params.put("dyMin", dyMin); }
        if (dyMax  != null) { where.append(" AND i.dividend_yield <= :dyMax"); params.put("dyMax", dyMax); }

        String baseSql = LIST_SELECT + where;
        String countSql = """
                SELECT COUNT(*) FROM COMPANY c
                LEFT JOIN STOCK_INDICATOR i  ON c.stock_code = i.stock_code
                LEFT JOIN STOCK_PRICE     sp ON c.stock_code = sp.srtn_cd
                    AND sp.bas_dt = (
                        SELECT MAX(sp2.bas_dt) FROM STOCK_PRICE sp2
                        WHERE sp2.srtn_cd = c.stock_code
                    )
                """ + where;

        Query dataQ  = entityManager.createNativeQuery(baseSql + orderClause(sort, dir));
        Query countQ = entityManager.createNativeQuery(countSql);
        params.forEach(dataQ::setParameter);
        params.forEach(countQ::setParameter);
        dataQ.setFirstResult(page * size);
        dataQ.setMaxResults(size);

        List<?> rows  = dataQ.getResultList();
        long    total = ((Number) countQ.getSingleResult()).longValue();
        return toPageResult(rows, total, page, size);
    }

    // ── 검색 ──────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getSerachResult(String keyword, int page, int size, String sort, String dir) throws Exception {
        String kw      = "%" + keyword + "%";
        String dataSql = LIST_SELECT + " WHERE c.corp_name LIKE :kw" + orderClause(sort, dir);
        String cntSql  = "SELECT COUNT(*) FROM COMPANY c WHERE c.corp_name LIKE :kw";

        List<?> rows = entityManager.createNativeQuery(dataSql)
                .setParameter("kw", kw)
                .setFirstResult(page * size).setMaxResults(size).getResultList();
        long total = ((Number) entityManager.createNativeQuery(cntSql)
                .setParameter("kw", kw).getSingleResult()).longValue();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object o : rows) list.add(rowToMap((Object[]) o));
        Map<String, Object> result = new HashMap<>();
        result.put("result",     list);
        result.put("totalCount", total);
        result.put("totalPages", size > 0 ? (int) Math.ceil((double) total / size) : 0);
        result.put("page",       page);
        return result;
    }
}
