package com.daisobook.shop.booksearch.BooksSearch.component;

import com.daisobook.shop.booksearch.BooksSearch.dto.RerankResult;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class RerankerClient {
    private static final String RERANKER_API = "http://reranker.java21.net/rerank";

    // Reranker API 호출 및 결과 반환
    public List<RerankResult> rerank(String query, List<String> texts) {
        // ... (RestTemplate 또는 WebClient를 이용한 HTTP POST 호출 로직)
        System.out.println("Reranker: API 호출하여 순서 재조정");

        // 더미 RerankResult 반환 (상위 3개)
        List<RerankResult> results = new ArrayList<>();
        results.add(new RerankResult(0, 0.9)); // texts[0]이 가장 높음
        results.add(new RerankResult(2, 0.8));
        results.add(new RerankResult(1, 0.7));
        return results;
    }
}