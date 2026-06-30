package com.example.demo.domain.service;

import com.example.demo.domain.dto.NewsDto;
import com.example.demo.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;

    public List<NewsDto> getNews(String stockCode) {
        return newsRepository.findByStockCodeOrderByPublishedAtDesc(stockCode)
                .stream()
                .map(NewsDto::from)
                .toList();
    }
}
