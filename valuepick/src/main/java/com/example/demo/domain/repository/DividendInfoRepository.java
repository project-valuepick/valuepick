package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DividendInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// PK가 corpCode(String) - 기존 Dividend.DividendId(복합키)에서 단순 String PK로 변경됨
@Repository
public interface DividendInfoRepository extends JpaRepository<DividendInfo, String> {

    // corpCode로 배당 정보 조회 - 지표 계산 시 배당수익률 가져올 때 사용
    Optional<DividendInfo> findByCorpCodeAndDividendKind(String corpCode, String dividendKind);

    // corpCode + dividendKind로 중복 체크 - 동일 데이터 재저장 방지
    boolean existsByCorpCodeAndDividendKind(String corpCode, String dividendKind);
}
