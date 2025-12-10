package com.daisobook.shop.booksearch.BooksSearch.search.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClient {

    private final WebClient webClient;

    @Value("${app.ai.embedding-url}")
    private String embeddingUrl;

    @Value("${app.ai.reranker-url}")
    private String rerankerUrl;

    @Value("${app.ai.gemini-url}")
    private String geminiUrl;

    @Value("${app.ai.gemini-api-key}")
    private String geminiApiKey;

    // --- 1. 임베딩 (Ollama) ---
    public List<Double> generateEmbedding(String text) {
        try {
            Map response = webClient.post().uri(embeddingUrl)
                    .bodyValue(Map.of("model", "bge-m3", "prompt", text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                    .block();
            return (List<Double>) response.get("embedding");
        } catch (Exception e) {
            log.error("임베딩 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // --- 2. 리랭킹 (Reranker) ---
    public List<Map<String, Object>> rerank(String query, List<String> texts) {
        try {
            return webClient.post().uri(rerankerUrl)
                    .bodyValue(Map.of("query", query, "texts", texts))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            log.warn("리랭킹 실패 (건너뜀): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // --- 3. 답변 생성 (Gemini) ---
    public String generateAnswer(String prompt) {
        try {
            GeminiRequest request = new GeminiRequest(
                    List.of(new Content(List.of(new Part(prompt))))
            );

            GeminiResponse response = webClient.post()
                    .uri(geminiUrl + "?key=" + geminiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(60))
//                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
//                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                    .block();

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }

            return "{}"; // 빈 JSON 반환

        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Gemini API 호출 한도 초과 (AI 추천 없이 진행)");

            return "{}";
        } catch (Exception e) {
            log.error("Gemini 호출 실패: {}", e.getMessage());
            return "{}";
        }
    }

    // --- Gemini DTO ---
    record GeminiRequest(List<Content> contents) {}
    record Content(List<Part> parts) {}
    record Part(String text) {}
    record GeminiResponse(List<Candidate> candidates) {}
    record Candidate(Content content) {}
}