package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.config.AiClient;
import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookWithScore;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.component.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCoreService {

    private final BookRepository bookRepository;
    private final AiClient aiClient;
    private final BookMapper bookMapper;

    private static final int ES_FETCH_SIZE = 50;
    private static final int RERANK_LIMIT = 10;
    private static final int FINAL_SIZE = 30;

    public SearchResponseDto executeSearch(String query) {
        long start = System.currentTimeMillis();

        // 1. ISBN 검색 (가장 빠름)
        if (query.matches("^[0-9-]+$")) {
            List<Book> isbnBooks = bookRepository.findByIsbn(query);
            if (!isbnBooks.isEmpty()) {
                return SearchResponseDto.builder()
                        .bookList(bookMapper.toDtoList(isbnBooks, 100))
                        .build();
            }
        }

        // 2. 키워드 정제
        String refinedQuery = SearchUtils.extractKeywords(query);

        // 3. 임베딩 생성 (실패 시 빈 리스트)
        List<Double> embeddingDouble = aiClient.generateEmbedding(refinedQuery);
        List<Float> embedding = embeddingDouble.stream().map(Double::floatValue).toList();

        // 4. ES 하이브리드 검색
        List<Book> candidates = bookRepository.searchHybrid(refinedQuery, embedding, ES_FETCH_SIZE);
        if (candidates.isEmpty()) return SearchResponseDto.empty();

        // 5. 리랭킹 (설명 줄여서 속도 확보)
        List<BookWithScore> rankedBooks;
        if (candidates.size() <= 3) {
            // 후보가 너무 적으면 리랭킹 의미 없음 -> 그냥 점수 부여
            rankedBooks = candidates.stream().map(b -> new BookWithScore(b, 0.9)).toList();
        } else {
            rankedBooks = applyRerankingSafe(refinedQuery, candidates);
        }

        // 6. 결과 반환 (최대 30개)
        List<BookResponseDto> dtos = rankedBooks.stream()
                .limit(FINAL_SIZE)
                .map(bs -> bookMapper.toDto(bs.book(), SearchUtils.calculateSigmoidScore(bs.score())))
                .collect(Collectors.toList());

        log.info("[Core] 검색 완료: {}ms (결과 {}건)", System.currentTimeMillis() - start, dtos.size());
        return SearchResponseDto.builder().bookList(dtos).build();
    }

    // 안전한 리랭킹 (설명 자르기 + 에러 시 원본 순서 유지)
    private List<BookWithScore> applyRerankingSafe(String query, List<Book> candidates) {
        long rerankStart = System.currentTimeMillis();
        try {
            // 1. 리랭킹 대상 10개만 선정
            List<Book> toRerank = candidates.stream().limit(RERANK_LIMIT).toList();
            List<Book> restBooks = candidates.stream().skip(RERANK_LIMIT).toList();

            // 2. 텍스트 생성 (핵심: 설명을 100자로 자름)
            List<String> docTexts = toRerank.stream()
                    .map(b -> {
                        String title = b.getTitle();
                        String category = Collections.singletonList(b.getCategories() != null ? b.getCategories() : "").toString();
                        String desc = SearchUtils.stripHtml(b.getDescription());

                        // [최적화] 설명이 100자를 넘어가면 자른다.
                        if (desc.length() > 100) {
                            desc = desc.substring(0, 100);
                        }

                        // 제목과 카테고리는 중요하니 다 넣고, 설명은 요약본만 보냄
                        return String.format("%s %s %s", title, category, desc);
                    })
                    .toList();

            // 3. API 호출
            List<Map<String, Object>> scores = aiClient.rerank(query, docTexts);

            if (scores.isEmpty()) throw new RuntimeException("Reranker 반환값 없음");

            // 4. 점수 매핑
            List<BookWithScore> finalResults = new ArrayList<>();
            for (int i = 0; i < toRerank.size(); i++) {
                double score = 0.0;
                if (i < scores.size()) {
                    Object s = scores.get(i).get("score");
                    score = (s instanceof Number n) ? n.doubleValue() : 0.0;
                }
                finalResults.add(new BookWithScore(toRerank.get(i), score));
            }

            // 5. 나머지 책들 (점수 0.0으로 뒤에 붙임)
            for (Book b : restBooks) {
                finalResults.add(new BookWithScore(b, 0.0));
            }

            // 6. 점수 높은 순 정렬
            finalResults.sort(Comparator.comparingDouble(BookWithScore::score).reversed());

            log.info("[Core] 리랭킹 성공 (10개): {}ms", System.currentTimeMillis() - rerankStart);
            return finalResults;

        } catch (Exception e) {
            log.warn("[Core] 리랭킹 실패 또는 시간 초과 -> ES 순서 유지: {}", e.getMessage());
            // 실패 시 ES가 준 순서 그대로 반환 (서비스는 죽지 않음)
            return candidates.stream()
                    .map(b -> new BookWithScore(b, 0.0))
                    .toList();
        }
    }
}