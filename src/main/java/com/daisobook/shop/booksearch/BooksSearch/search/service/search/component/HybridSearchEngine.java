package com.daisobook.shop.booksearch.BooksSearch.search.service.search.component;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookWithScore;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.AiProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HybridSearchEngine {

    private final BookRepository bookRepository;
    private final AiProviderService aiProvider;

    private static final int ES_FETCH_SIZE = 50;  // ES에서 가져올 후보 수
    private static final int RERANK_LIMIT = 20;   // 실제 리랭킹할 상위 개수 (비용 절약)

    public List<BookWithScore> searchAndRerank(String query) {
        // 1. 임베딩 (Redis 캐싱은 AiProvider 내부에서 처리 권장)
        List<Float> embedding = aiProvider.generateEmbedding(query);

        // 2. ES 검색 (50개 확보)
        List<Book> candidates = bookRepository.searchHybrid(query, embedding, ES_FETCH_SIZE);
        if (candidates.isEmpty()) return Collections.emptyList();

        // 3. 리랭킹 대상(Top 20)과 나머지(Rest 30) 분리
        List<Book> toRerank = candidates.stream().limit(RERANK_LIMIT).toList();
        List<Book> restBooks = candidates.stream().skip(RERANK_LIMIT).toList();

        // 4. 리랭킹 실행 (20개만 하므로 빠름)
        List<Map<String, Object>> scores = aiProvider.rerank(query, toRerank);

        // 5. 결과 재조립
        List<BookWithScore> finalResults = new ArrayList<>();

        // A. 리랭킹된 그룹 (점수 높음)
        for (int i = 0; i < scores.size() && i < toRerank.size(); i++) {
            double newScore = extractScore(scores.get(i));
            finalResults.add(new BookWithScore(toRerank.get(i), newScore));
        }

        // B. 리랭킹 안 된 그룹 (점수 낮음 - 0점 처리하거나 ES 점수 유지)
        // 여기선 리랭킹 그룹 뒤에 붙이기 위해 0.0점 이하로 처리
        for (Book book : restBooks) {
            finalResults.add(new BookWithScore(book, 0.0));
        }

        // 6. 점수순 정렬
        finalResults.sort(Comparator.comparingDouble(BookWithScore::score).reversed());

        return finalResults;
    }

    private double extractScore(Map<String, Object> map) {
        return (map.get("score") instanceof Number n) ? n.doubleValue() : 0.0;
    }
}