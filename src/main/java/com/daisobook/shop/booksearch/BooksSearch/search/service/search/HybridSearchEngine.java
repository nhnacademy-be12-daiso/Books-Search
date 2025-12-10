package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookWithScore;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class HybridSearchEngine {

    private final BookRepository bookRepository;
    private final AiProviderService aiProvider;

    /**
     * 임베딩 -> 하이브리드 검색 -> 리랭킹 -> 정렬까지 한 번에 수행
     */
    public List<BookWithScore> searchAndRerank(String query, int size) {
        // 1. 임베딩
        List<Float> embedding = aiProvider.generateEmbedding(query);

        // 2. ES 검색
        List<Book> candidates = bookRepository.searchHybrid(query, embedding, size);
        if (candidates.isEmpty()) return Collections.emptyList();

        // 3. 리랭킹 (Reranker)
        List<Map<String, Object>> scores = aiProvider.rerank(query, candidates);

        // 4. 점수 매핑 및 정렬
        if (scores.isEmpty()) {
            return candidates.stream().map(b -> new BookWithScore(b, 0.0)).toList();
        }

        List<BookWithScore> result = new ArrayList<>();
        // 후보군과 점수 리스트의 순서가 같다고 가정 (Rerank API 특성)
        for (int i = 0; i < scores.size() && i < candidates.size(); i++) {
            double score = extractScore(scores.get(i));
            result.add(new BookWithScore(candidates.get(i), score));
        }

        result.sort(Comparator.comparingDouble(BookWithScore::score).reversed());
        return result;
    }

    private double extractScore(Map<String, Object> map) {
        return (map.get("score") instanceof Number n) ? n.doubleValue() : 0.0;
    }
}