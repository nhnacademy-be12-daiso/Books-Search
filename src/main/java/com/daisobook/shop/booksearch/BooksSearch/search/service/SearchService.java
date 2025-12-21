package com.daisobook.shop.booksearch.BooksSearch.search.service;

import com.daisobook.shop.booksearch.BooksSearch.search.component.CacheKeyGenerator;
import com.daisobook.shop.booksearch.BooksSearch.search.component.QueryPreprocessor;
import com.daisobook.shop.booksearch.BooksSearch.search.component.ai.EmbeddingClient;
import com.daisobook.shop.booksearch.BooksSearch.search.component.assembler.SearchResultAssembler;
import com.daisobook.shop.booksearch.BooksSearch.search.component.engine.ElasticsearchEngine;
import com.daisobook.shop.booksearch.BooksSearch.search.component.mq.BookSearchTaskPublisher;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiAnalysisDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchEngine elasticsearchEngine;
    private final EmbeddingClient embeddingClient;

    private final SearchResultAssembler assembler;
    private final QueryPreprocessor queryPreprocessor;
    private final CacheKeyGenerator keyGenerator;
    private final RedisCacheService redisCacheService;

    private final PendingWorkService pendingWorkService;
    private final BookSearchTaskPublisher taskPublisher;

    private static final int BATCH_SIZE = 3;

    /**
     * 통합 검색:
     * - (1) 임베딩 생성 (실패 시 키워드만)
     * - (2) ES 하이브리드 검색
     * - (3) 결과 중 aiResult 없는 책:
     *       - Redis Set(임베딩 후보) 적재
     *       - MQ로 AI 분석 요청(쿨다운 적용)
     */
    public SearchResponseDto search(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return SearchResponseDto.empty();
        }

        // ISBN 패턴이면 단건 조회로 빠르게
        if (userQuery.matches("^[0-9-]+$")) {
            List<Book> books = elasticsearchEngine.searchByIsbn(userQuery);
            return assembler.assembleBasicResult(books);
        }

        // 캐시 조회
        String cacheKey = keyGenerator.generateKey("search", userQuery);
        SearchResponseDto cached = redisCacheService.get(cacheKey, SearchResponseDto.class);
        if (cached != null) return cached;

        // 전처리: 키워드 추출
        log.info("[SEARCH] cache miss. userQuery='{}'", userQuery);

        String refinedQuery = queryPreprocessor.extractKeywords(userQuery);
        log.info("[SEARCH] refinedQuery='{}' (len={})", refinedQuery, refinedQuery == null ? -1 : refinedQuery.length());


        // (1) 임베딩 생성
        List<Float> embedding;
        try {
            embedding = embeddingClient.createEmbedding(refinedQuery);
        } catch (Exception e) {
            log.warn("⚠️ [Fallback] 임베딩 실패(키워드만): {}", e.getMessage());
            embedding = Collections.emptyList();
        }

        // (2) ES 하이브리드 검색
        List<Book> books = elasticsearchEngine.search(refinedQuery, embedding);

        List<String> missingAiIsbns = new ArrayList<>();

        for (Book b : books) {
            String isbn = b.getIsbn();
            if (!StringUtils.hasText(isbn)) continue; // ISBN 없으면 스킵

            AiAnalysisDto aiResult = b.getAiResult();

            boolean needAnalysis = aiResult == null ||
                    ObjectUtils.isEmpty(aiResult.pros()) ||
                    ObjectUtils.isEmpty(aiResult.cons()) ||
                    ObjectUtils.isEmpty(aiResult.recommendedFor());

            // (2-1) AI 분석 결과 누락된 도서들 선별
            if (needAnalysis) {
                // 너무 자주 발행되지 않도록 쿨다운 체크
                if (pendingWorkService.canPublishAi(isbn)) {
                    // aiResult 누락 도서 List에 추가
                    missingAiIsbns.add(isbn);
                    log.info("🎯 [Target] AI Analysis Scheduled: ISBN={}", isbn);
                } else {
                    // 쿨다운 중이라면 스킵
                    log.debug("⏳ [Cooldown] AI Analysis skipped (Already queued/processed): ISBN={}", isbn);
                }
            }
        }

        // (2-2) MQ로 AI 분석 작업 발행 (배치 처리)
        if (!missingAiIsbns.isEmpty()) {
            log.info("🚀 [Publish] Sending {} books to RabbitMQ", missingAiIsbns.size());
            // 배치 발행
            publishBatches(missingAiIsbns);
        } else {
            // 아무것도 안 잡혔다면 이유를 알기 위해 로그
            log.info("💤 [Skip] No books require AI analysis this time.");
        }


        // (3) 결과 조립
        SearchResponseDto result = assembler.assembleBasicResult(books);

        // 캐시: 너무 길게 잡지 말고 5~15분 권장(검색 로그/변동 반영)
        redisCacheService.save(cacheKey, result, Duration.ofMinutes(10));
        return result;
    }

    // 리스트 분할 발행 로직
    private void publishBatches(List<String> isbns) {
        if (isbns == null || isbns.isEmpty()) return;

        for (int i = 0; i < isbns.size(); i += BATCH_SIZE) {
            int end = Math.min(isbns.size(), i + BATCH_SIZE);
            List<String> batch = new ArrayList<>(isbns.subList(i, end)); // 안전한 복사

            try {
                taskPublisher.publishAiAnalysisBatch(batch);
                log.info("[Search] Published AI Batch size={}", batch.size());
            } catch (Exception ex) {
                log.warn("[Search] Failed to publish AI batch", ex);
            }
        }
    }
}
