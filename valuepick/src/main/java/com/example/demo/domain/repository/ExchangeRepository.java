package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, String> {
    List<Exchange> findAllByOrderByBaseDate();
}
