package com.example.demo.domain.parser;

import com.example.demo.domain.dto.StockPriceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class StockPriceXmlParser {

    public StockPriceDto parse(String xml) {

        try {
            Element item = getFirstItem(xml);

            // 데이터가 없으면 null 반환 (거래 없는 날 등)
            if (item == null) return null;

            // "basDt" XML 태그값 "20240101" → LocalDate 변환
            String basDtStr = get(item, "basDt");
            LocalDate basDt = (basDtStr != null && !basDtStr.isBlank())
                    ? LocalDate.parse(basDtStr, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    : null;

            // 새 StockPriceDto 필드명 기준으로 매핑
            return StockPriceDto.builder()
                    .srtnCd(get(item, "srtnCd"))                    // 종목코드
                    .basDt(basDt)                                   // 기준일 (String → LocalDate 변환)
                    .clpr(parseLong(get(item, "clpr")))             // 종가
                    .mkp(parseLong(get(item, "mkp")))               // 시가
                    .fltRt(parseDouble(get(item, "fltRt")))         // 등락률
                    .lstgStCnt(parseLong(get(item, "lstgStCnt")))   // 상장주식수 (발행주식수 대체)
                    .mrktTotAmt(parseLong(get(item, "mrktTotAmt"))) // 시가총액
                    .createdAt(LocalDateTime.now())                  // 저장 시각
                    .updatedAt(LocalDateTime.now())                  // 수정 시각
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("StockPrice XML 파싱 실패", e);
        }
    }


    // XML에서 첫 번째 <item> 엘리먼트 추출 - parse()와 parseMrktCtg() 공통 사용
    private Element getFirstItem(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagName("item");
        log.info("ITEM SIZE = {}", items.getLength());

        if (items.getLength() == 0) return null;
        return (Element) items.item(0);
    }

    // XML 태그 값 추출 유틸
    private String get(Element item, String tag) {
        NodeList list = item.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }

    // 문자열 → Long 변환 (콤마 제거, null/빈값은 null 반환)
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 문자열 → Double 변환 (등락률용, null/빈값은 null 반환)
    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}