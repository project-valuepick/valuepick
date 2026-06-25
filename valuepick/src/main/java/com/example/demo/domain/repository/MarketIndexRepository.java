package com.example.demo.domain.repository;

import com.example.demo.domain.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {
    Optional<MarketIndex> findTop1ByIdxNmOrderByBasDdDesc(String idxNm);
}
