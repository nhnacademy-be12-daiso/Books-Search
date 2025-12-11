package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiResultDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookWithScore;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.component.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.component.CacheKeyGenerator;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.component.HybridSearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final HybridSearchEngine searchEngine;
    private final AiProviderService aiProvider;
    private final BookMapper bookMapper;

    // Redis & Utils
    private final RedisCacheService redisCacheService;
    private final CacheKeyGenerator keyGenerator;

    private static final int FINAL_RESULT_SIZE = 30; // 사용자에게 보여줄 최종 개수 (3페이지 분량)
    private static final int AI_EVAL_SIZE = 5;       // LLM 평가 맡길 개수

    // 1. 일반 검색 (Basic)
    public SearchResponseDto basicSearch(String userQuery) {
        // ISBN 검색은 캐싱 제외 (너무 명확하므로)
        if (userQuery.matches("^[0-9-]+$")) {
            List<Book> isbnBooks = bookRepository.findByIsbn(userQuery);
            if (!isbnBooks.isEmpty()) {
                return SearchResponseDto.builder().bookList(bookMapper.toDtoList(isbnBooks, 100)).build();
            }
        }

        // [Redis] 1. 캐시 확인
        String cacheKey = keyGenerator.generateKey("basic", userQuery);
        SearchResponseDto cachedResult = redisCacheService.get(cacheKey, SearchResponseDto.class);
        if (cachedResult != null) return cachedResult;

        // [Logic] 2. 검색 수행 (Rerank Top 20 적용됨)
        String refinedQuery = SearchUtils.extractKeywords(userQuery);
        List<BookWithScore> scoredBooks = searchEngine.searchAndRerank(refinedQuery);

        // 상위 30개 자르기
        List<BookWithScore> finalBooks = scoredBooks.stream().limit(FINAL_RESULT_SIZE).toList();

        // DTO 변환
        List<BookResponseDto> dtos = finalBooks.stream()
                .map(bs -> bookMapper.toDto(bs.book(), SearchUtils.calculateSigmoidScore(bs.score())))
                .toList();

        SearchResponseDto result = SearchResponseDto.builder().bookList(dtos).build();

        // [Redis] 3. 캐시 저장
        redisCacheService.save(cacheKey, result, Duration.ofHours(1));

        return result;
    }

    // 2. AI 검색 (AI)
    public SearchResponseDto aiSearch(String userQuery) {
        // [Redis] 1. 캐시 확인
        String cacheKey = keyGenerator.generateKey("ai", userQuery);
        SearchResponseDto cachedResult = redisCacheService.get(cacheKey, SearchResponseDto.class);
        if (cachedResult != null) return cachedResult;

        // [Logic] 2. 검색 수행
        String refinedQuery = SearchUtils.extractKeywords(userQuery);
        List<BookWithScore> scoredBooks = searchEngine.searchAndRerank(refinedQuery); // 이미 정렬되어 나옴

        if (scoredBooks.isEmpty()) return SearchResponseDto.empty();

        // 3. AI 평가 전략: 상위 5개만 평가, 나머지는 일반 결과로 채움 (총 30개)
        List<Book> top5Books = scoredBooks.stream().limit(AI_EVAL_SIZE).map(BookWithScore::book).toList();

        // 상위 5개에 대해서만 LLM 호출 (느림)
        Map<String, AiResultDto> aiResults = aiProvider.evaluateBooks(userQuery, top5Books);

        // 4. 결과 합치기 (Top 30)
        List<BookWithScore> finalTarget = scoredBooks.stream().limit(FINAL_RESULT_SIZE).toList();
        List<BookResponseDto> dtos = bookMapper.toDtoList(finalTarget.stream().map(BookWithScore::book).toList(), 0);

        // AI 결과 매핑 (상위 5개는 matchRate와 멘트가 들어가고, 나머지는 null/0점)
        bookMapper.applyAiEvaluation(dtos, aiResults);

        // (선택) AI 점수가 높은 순으로 다시 정렬 (LLM이 점수를 낮게 줬을 수도 있으므로)
        dtos.sort(Comparator.comparingInt(BookResponseDto::getMatchRate).reversed());

        SearchResponseDto result = SearchResponseDto.builder().bookList(dtos).build();

        // [Redis] 5. 캐시 저장
        redisCacheService.save(cacheKey, result, Duration.ofHours(12));

        return result;
    }

}