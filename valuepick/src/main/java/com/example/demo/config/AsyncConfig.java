package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // @Async 어노테이션 활성화
public class AsyncConfig {

    // 주가 수집용 스레드풀 - parallelStream 내부에서 사용
    // corePoolSize=10: 기본 10개 스레드 유지
    // maxPoolSize=20: 최대 20개까지 확장 가능
    // queueCapacity=500: 대기 작업 최대 500개
    @Bean("stockExecutor")
    public Executor stockExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("stock-");
        executor.initialize();
        return executor;
    }

    // DART API용 스레드풀 - @Async 메서드 비동기 실행용
    // DART API 호출 제한 고려해서 스레드 수를 작게 유지 (IP 차단 방지)
    // corePoolSize=2: 동시에 2개 수집 작업만 실행 (재무 + 배당 동시 실행 가능)
    // maxPoolSize=2: 절대로 2개 초과 안 함 (DART API 보호)
    // queueCapacity=10: 대기 작업 최대 10개
    @Bean("dartExecutor")
    public Executor dartExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("dart-");
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

}
