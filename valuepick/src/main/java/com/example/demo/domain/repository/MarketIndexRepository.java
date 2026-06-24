package com.example.demo.domain.repository;


import com.example.demo.domain.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {
}
