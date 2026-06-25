package com.example.demo.domain.service;

import com.example.demo.domain.entity.Company;
import com.example.demo.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DartCompanyCollector {

    private final CompanyRepository companyRepository;
    private final RestTemplate restTemplate;

    @Value("${dart.api.key}")
    private String apiKey;

    private static final int SLEEP_MS = 100; // company.json 개별 호출이라 넉넉하게 설정
    private static final String COMPANY_URL = "https://opendart.fss.or.kr/api/company.json";

    // @Async로 백그라운드 실행 - 컨트롤러가 즉시 응답 반환 가능
    // dartExecutor 스레드풀 사용 - DART API 호출 제한 고려

    @Async("dartExecutor")
    public void collectCompanies() {

        try {

            // 1단계: corpCode.xml에서 상장사 corpCode 목록 추출
            List<String> corpCodes = collectCorpCodes();
            log.info("상장사 corpCode 수집 완료: {}건", corpCodes.size());

            // 기존 데이터 전체 삭제 - 루프 시작 전 한 번만 실행
            companyRepository.deleteAllInBatch();

            int savedCount = 0;

            // 2단계: 각 corpCode로 company.json 호출해서 Company 엔티티 하나씩 즉시 저장
            for (String corpCode : corpCodes) {

                try {

                    // company.json API 호출 - corpCode로 개별 기업 상세 조회
                    String url = COMPANY_URL + "?crtfc_key=" + apiKey + "&corp_code=" + corpCode;
                    Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                    // 응답 없거나 실패면 스킵
                    if (response == null || !"000".equals(response.get("status"))) {
                        log.warn("API 실패: {} - {}", corpCode, response != null ? response.get("status") : "null");
                        continue;
                    }

                    // company.json 응답에서 필요한 값 추출
                    String stockCode = (String) response.get("stock_code"); // 종목코드
                    String corpName  = (String) response.get("corp_name");  // 회사명
                    String corpCls   = (String) response.get("corp_cls");   // 법인구분 Y:코스피, K:코스닥, N:코넥스, E:기타

                    // 종목코드 없으면 비상장사이므로 제외
                    if (stockCode == null || stockCode.isBlank()) continue;

                    // Company 엔티티 생성 후 즉시 저장
                    Company company = Company.builder()
                            .stockCode(stockCode)
                            .corpCode(corpCode)
                            .corpName(corpName)
                            .corpCls(corpCls)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    companyRepository.save(company); // 하나씩 즉시 저장
                    savedCount++;
                    log.info("기업정보 저장 완료: {} {} {} (total={})", corpName, stockCode, corpCls, savedCount);

                    Thread.sleep(SLEEP_MS); // DART API 호출 제한 방지

                } catch (Exception e) {
                    log.error("기업 처리 실패: {}", corpCode, e);
                }
            }

            log.info("전체 기업정보 저장 완료: {}건", savedCount);

        } catch (Exception e) {
            log.error("기업정보 수집 실패", e);
            throw new RuntimeException(e);
        }
    }

    // corpCode.xml에서 상장사 corpCode 목록만 추출
    private List<String> collectCorpCodes() throws Exception {

        // DART 기업목록 ZIP 파일 다운로드
        String url = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + apiKey;
        byte[] zipData = restTemplate.getForObject(url, byte[].class);

        if (zipData == null) throw new RuntimeException("DART 응답 없음");

        // ZIP 압축 해제 후 XML 파싱
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData));
        zis.getNextEntry();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(zis);

        NodeList nodeList = document.getElementsByTagName("list");

        List<String> corpCodes = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);

            // 종목코드 확인 - 없으면 비상장사이므로 제외
            String stockCode = element.getElementsByTagName("stock_code")
                    .item(0).getTextContent().trim();

            if (stockCode.isBlank()) continue;

            // 상장사만 corpCode 추출
            String corpCode = element.getElementsByTagName("corp_code")
                    .item(0).getTextContent().trim();

            corpCodes.add(corpCode);
        }

        return corpCodes;
    }
}