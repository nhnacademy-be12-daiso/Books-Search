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

    private static final int ES_FETCH_SIZE = 50;
    private static final int RERANK_LIMIT = 10;

    // 일반 검색 (리랭킹 X -> ES 점수 사용)
    public List<BookWithScore> searchWithoutRerank(String query) {
        List<Float> embedding = aiProvider.generateEmbedding(query);
        List<Book> candidates = bookRepository.searchHybrid(query, embedding, ES_FETCH_SIZE);

        if (candidates.isEmpty()) return Collections.emptyList();

        List<BookWithScore> finalResults = new ArrayList<>();
        for (Book book : candidates) {
            // 리랭킹을 안 하므로 기본 점수(혹은 ES score) 부여
            finalResults.add(new BookWithScore(book, 0.5));
        }
        return finalResults;
    }

    // AI 검색 (리랭킹 O)
    public List<BookWithScore> searchAndRerank(String query) {
        List<Float> embedding = aiProvider.generateEmbedding(query);
        List<Book> candidates = bookRepository.searchHybrid(query, embedding, ES_FETCH_SIZE);
        if (candidates.isEmpty()) return Collections.emptyList();

        // 1. 리랭킹 대상 (Top 10)
        List<Book> toRerank = candidates.stream().limit(RERANK_LIMIT).toList();
        List<Book> restBooks = candidates.stream().skip(RERANK_LIMIT).toList();

        // 2. 리랭킹 실행 (10개라 빠름)
        List<Map<String, Object>> scores = aiProvider.rerank(query, toRerank);

        // 3. 결과 재조립
        List<BookWithScore> finalResults = new ArrayList<>();

        for (int i = 0; i < scores.size() && i < toRerank.size(); i++) {
            double newScore = extractScore(scores.get(i));
            finalResults.add(new BookWithScore(toRerank.get(i), newScore));
        }

        // 나머지는 0점 처리하여 뒤로 보냄
        for (Book book : restBooks) {
            finalResults.add(new BookWithScore(book, 0.0));
        }

        finalResults.sort(Comparator.comparingDouble(BookWithScore::score).reversed());
        return finalResults;
    }

    private double extractScore(Map<String, Object> map) {
        return (map.get("score") instanceof Number n) ? n.doubleValue() : 0.0;
    }
}