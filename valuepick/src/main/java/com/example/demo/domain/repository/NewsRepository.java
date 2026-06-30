package com.example.demo.domain.repository;

import com.example.demo.domain.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    boolean existsByStockCodeAndOfficeIdAndArticleId(String stockCode, String officeId, String articleId);

    List<News> findByStockCodeOrderByPublishedAtDesc(String stockCode);
}
