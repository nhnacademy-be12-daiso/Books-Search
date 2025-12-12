package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.component.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchCoreService searchCoreService;
    private final AiEnrichmentService aiEnrichmentService;
    private final RedisCacheService redisCacheService;
    private final CacheKeyGenerator keyGenerator;

    /**
     * 일반 검색: 검색 -> DTO 변환 (AI 평가 X, 빠름)
     */
    public SearchResponseDto basicSearch(String query) {
        // 1. 캐시 조회
        String cacheKey = keyGenerator.generateKey("basic", query);
        SearchResponseDto cached = redisCacheService.get(cacheKey, SearchResponseDto.class);
        if (cached != null) {
            log.info("[Facade] Basic 캐시 적중: {}", query);
            return cached;
        }

        // 2. 코어 검색 실행
        SearchResponseDto result = searchCoreService.executeSearch(query);

        // 3. 캐시 저장 (1시간)
        redisCacheService.save(cacheKey, result, Duration.ofHours(1));
        return result;
    }

    /**
     * AI 검색: 검색 -> 상위권 AI 분석 -> 병합 (느림, 정보 풍부)
     */
    public SearchResponseDto aiSearch(String query) {
        // 1. 캐시 조회
        String cacheKey = keyGenerator.generateKey("ai", query);
        SearchResponseDto cached = redisCacheService.get(cacheKey, SearchResponseDto.class);
        if (cached != null) {
            log.info("[Facade] AI 캐시 적중: {}", query);
            return cached;
        }

        // 2. 코어 검색 실행 (Basic과 동일한 로직 재사용)
        SearchResponseDto basicResult = searchCoreService.executeSearch(query);

        if (basicResult.getBookList().isEmpty()) {
            return basicResult;
        }

        // 3. AI 평가 (Enrichment) 실행 - 여기가 느린 구간
        //    Basic 결과에 AI 설명을 덧붙여서 새로운 결과를 만듦
        SearchResponseDto enrichedResult = aiEnrichmentService.enrichWithAiAnalysis(query, basicResult);

        // 4. 캐시 저장 (12시간 - AI 비용 절감)
        redisCacheService.save(cacheKey, enrichedResult, Duration.ofHours(12));
        return enrichedResult;
    }
}