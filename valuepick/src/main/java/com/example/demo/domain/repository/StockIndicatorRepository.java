package com.example.demo.domain.repository;

import com.example.demo.domain.entity.StockIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockIndicatorRepository extends JpaRepository<StockIndicator, String> {
}
