package com.example.demo.domain.service;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.entity.Exchange;
import com.example.demo.domain.repository.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateApiService {

    private final ExchangeRepository exchangeRepository;
    private final RestTemplate restTemplate; // RestTemplateConfig에서 @Bean으로 등록된 빈 주입

    @Value("${koreaexim.api.key}")
    private String koreaeximApiKey;

    // 한국수출입은행 환율 API cur_unit 코드
//    private static final String JAPAN_CUR_UNIT = "JPY(100)";
//    private static final String USA_CUR_UNIT = "USD";
//    private static final String CHINA_CUR_UNIT = "CNH";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_PREVIOUS_DAY_LOOKUP = 10; // 연휴가 길어도 이전 영업일을 찾을 수 있도록 최대 10일 탐색

    // 특정 날짜 환율 수집 후 DB 저장 - ExchangeDto 리스트로 반환
    @Transactional
    public List<ExchangeDto> fetchAndSaveExchangeRates(String searchDate) {
        return getExchangeRateChanges(searchDate);
    }


    // 오늘 기준 영업일 환율 수집 후 DB 저장
    @Transactional
    public List<ExchangeDto> fetchAndSaveExchangeRatesForToday() {
        return fetchAndSaveExchangeRates(LocalDate.now().format(DATE_FORMAT));
    }

    // 기준일자 환율을 전 영업일과 비교해 changeRate, changeAmount 계산 후 저장
    // Exchange 엔티티에 changeRate, changeAmount 필드가 있으므로 별도 DTO 없이 처리
    @Transactional
    public List<ExchangeDto> getExchangeRateChanges(String searchDate) {

        // 기준일자 환율 수집 및 저장
        List<Exchange> currentRates = callExchangeApi(searchDate);

        // 전 영업일 환율 조회 (DB 저장 안 함)
        List<Exchange> previousRates = findPreviousBusinessDayRates(
                LocalDate.parse(searchDate, DATE_FORMAT));

        // 전 영업일 데이터를 country 기준으로 맵 구성
        Map<String, Exchange> previousByCountry = previousRates.stream()
                .collect(Collectors.toMap(Exchange::getCountry, e -> e));

        List<Exchange> result = new ArrayList<>();

        for (Exchange current : currentRates) {

            Exchange previous = previousByCountry.get(current.getCountry());

            if (previous != null) {
                // changeAmount = 현재 환율 - 전일 환율
                double changeAmount = current.getDealBasR() - previous.getDealBasR();
                // changeRate = 등락폭 / 전일 환율 × 100
                double changeRate = (changeAmount / previous.getDealBasR()) * 100;

                // Exchange 엔티티에 changeRate, changeAmount 바로 저장
                result.add(Exchange.builder()
                        .curUnit(current.getCurUnit())
                        .baseDate(current.getBaseDate())
                        .country(current.getCountry())
                        .dealBasR(current.getDealBasR())
                        .changeAmount(changeAmount)  // 전일 대비 등락폭
                        .changeRate(changeRate)       // 전일 대비 등락률(%)
                        .build());
            } else {
                result.add(current);
            }
        }

        // 계산된 데이터 저장
        List<Exchange> saved = exchangeRepository.saveAll(result);
        return saved.stream().map(ExchangeDto::from).collect(Collectors.toList());
    }

    // searchDate 이전 영업일을 하루씩 거슬러 올라가며 환율 정보 탐색
    // 주말/공휴일은 API가 빈 응답을 주므로 건너뜀
    private List<Exchange> findPreviousBusinessDayRates(LocalDate searchDate) {

        LocalDate candidate = searchDate.minusDays(1);

        for (int attempt = 0; attempt < MAX_PREVIOUS_DAY_LOOKUP; attempt++) {

            try {
                // DB에서 먼저 조회 - 없으면 API 호출
                List<Exchange> dbRates = exchangeRepository.findByBaseDate(candidate);

                if (!dbRates.isEmpty()) {
                    log.info("전 영업일 DB 조회 성공: {}", candidate);
                    return dbRates;
                }

                // DB에 없으면 API 호출 (저장은 안 함)
                return callExchangeApi(candidate.format(DATE_FORMAT));

            } catch (IllegalStateException e) {
                candidate = candidate.minusDays(1); // 주말/공휴일이면 하루 더 거슬러 올라감
            }
        }

        throw new IllegalStateException("전 영업일 환율 정보를 찾을 수 없습니다 (최대 " + MAX_PREVIOUS_DAY_LOOKUP + "일 탐색).");
    }

    // 한국수출입은행 환율 API 호출 → Exchange 엔티티 리스트 반환 (DB 저장은 호출자 책임)
    private List<Exchange> callExchangeApi(String searchDate) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON")
                .queryParam("authkey", koreaeximApiKey)
                .queryParam("searchdate", searchDate)
                .queryParam("data", "AP01")
                .build(true)
                .toUri();

        List<Map<String, Object>> response = restTemplate.getForObject(uri, List.class);

        // 주말/공휴일이거나 데이터 없으면 예외 발생
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("해당 날짜(" + searchDate + ")는 주말/공휴일이거나 환율 정보가 없습니다.");
        }

        Object firstResult = response.get(0).get("result");
        if (firstResult != null && !"1".equals(String.valueOf(firstResult))) {
            throw new IllegalStateException("환율 API 호출 실패 (result 코드: " + firstResult + ")");
        }

        // cur_unit 기준으로 빠르게 조회하기 위한 맵 구성
        Map<String, Map<String, Object>> itemsByCurUnit = new LinkedHashMap<>();
        for (Map<String, Object> item : response) {
            Object curUnit = item.get("cur_unit");
            if (curUnit != null) {
                itemsByCurUnit.put(String.valueOf(curUnit), item);
            }
        }

        // 필요한 통화만 추출해서 Exchange 엔티티 생성
        // searchDate String → LocalDate 변환
        LocalDate baseDate = LocalDate.parse(searchDate, DATE_FORMAT);

        // 변경 - 전체 통화 처리
        List<Exchange> result = new ArrayList<>();
        result.add(buildKoreaExchange(baseDate)); // 한국은 API 응답에 없어서 직접 생성

        for (Map<String, Object> item : response) {
            try {
                result.add(buildExchange(
                        String.valueOf(item.get("cur_nm")), // country로 cur_nm 사용
                        item,
                        baseDate));
            } catch (Exception e) {
                log.warn("환율 파싱 실패: {}", item.get("cur_unit"));
            }
        }

        return result;
    }

    // 한국(KRW) 환율 엔티티 생성 - 기준 화폐라 환율은 1.0 고정
    private Exchange buildKoreaExchange(LocalDate baseDate) {
        return Exchange.builder()
                .curUnit("KRW")
                .baseDate(baseDate)
                .country("KOREA")
                .dealBasR(1.0)
                .changeAmount(0.0)
                .changeRate(0.0)
                .build();
    }

    // API 응답에서 Exchange 엔티티 생성
    // ttb, tts, curNm은 새 Exchange 엔티티에 없으므로 제거
    private Exchange buildExchange(String country, Map<String, Object> item, LocalDate baseDate) {

        if (item == null) {
            throw new IllegalStateException(country + "의 환율 정보를 응답에서 찾을 수 없습니다.");
        }

        return Exchange.builder()
                .curUnit(String.valueOf(item.get("cur_unit")))
                .baseDate(baseDate)                              // String → LocalDate 변환된 값 사용
                .country(country)
                .dealBasR(parseRate(item.get("deal_bas_r")))    // 매매기준율
                .changeAmount(null)                              // 전일 비교 후 별도 업데이트
                .changeRate(null)                                // 전일 비교 후 별도 업데이트
                .build();
    }

    // "1,313.00" 형태의 콤마 포함 환율 문자열 → Double 변환
    private Double parseRate(Object raw) {
        if (raw == null) return null;
        String text = String.valueOf(raw).replace(",", "").trim();
        if (text.isBlank()) return null;
        return Double.parseDouble(text);
    }
}
