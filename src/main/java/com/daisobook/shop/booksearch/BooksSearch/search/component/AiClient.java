package com.daisobook.shop.booksearch.BooksSearch.search.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// AI 서비스 연동 클라이언트
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${app.ai.embedding-url}")
    private String embeddingUrl;

    /**
     * 텍스트 임베딩 생성
     * @param text 입력 텍스트
     * @return 임베딩 벡터 리스트
     */
    public List<Double> generateEmbedding(String text) {
        try {
            Map response = webClient.post().uri(embeddingUrl)
                    .bodyValue(Map.of("model", "bge-m3", "prompt", text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    // .timeout(...) <-- 제거됨
                    .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(300)))
                    .block();
            return (List<Double>) response.get("embedding");
        } catch (Exception e) {
            log.error("[AiClient] 임베딩 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}