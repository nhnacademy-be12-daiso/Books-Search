package com.daisobook.shop.booksearch.BooksSearch.search.component.ai;

import com.daisobook.shop.booksearch.BooksSearch.search.component.AiClient;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.exception.RerankingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RerankingClient {
    private final AiClient aiClient;

    public List<Map<String, Object>> rerank(String query, List<Book> candidates) {
        try {
            // 텍스트 변환 로직도 여기에 숨김
            List<String> docTexts = candidates.stream()
                    .map(b -> b.getTitle() + " " + stripHtml(b.getDescription()))
                    .toList();
            return aiClient.rerank(query, docTexts);
        } catch (Exception e) {
            log.error("[RerankingClient] 리랭킹 실패. Query: {}", query, e);
            throw new RerankingException("Rerank API 호출 오류", e);
        }
    }

    // 간단한 문자열 처리는 private으로 내부에 둠
    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").substring(0, Math.min(html.length(), 50));
    }
}