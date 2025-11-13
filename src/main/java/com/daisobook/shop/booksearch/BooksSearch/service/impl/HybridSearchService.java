package com.daisobook.shop.booksearch.BooksSearch.service.impl;

import com.daisobook.shop.booksearch.BooksSearch.component.ElasticsearchClient;
import com.daisobook.shop.booksearch.BooksSearch.component.GeminiLLMClient;
import com.daisobook.shop.booksearch.BooksSearch.component.RerankerClient;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookDocument;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookSearchRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookSearchResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.RerankResult;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private final ElasticsearchClient esClient;
    private final RerankerClient rerankerClient;
    private final GeminiLLMClient geminiClient;

    // 생성자 주입
    public HybridSearchService(ElasticsearchClient esClient, RerankerClient rerankerClient, GeminiLLMClient geminiClient) {
        this.esClient = esClient;
        this.rerankerClient = rerankerClient;
        this.geminiClient = geminiClient;
    }

    // 1. 하이브리드 검색의 메인 메서드
    public BookSearchResponse searchBooks(BookSearchRequest request) {
        String query = request.getQuery();
        
        // 1. 검색 (Retrieval): 키워드 + 벡터 검색
        // 키워드와 RAG 쿼리가 모두 없으면 인기순 정렬만 수행
        if (query == null && request.getKeyword() == null) {
            // 키워드나 RAG 쿼리가 없는 경우, 단순 정렬/필터링 검색 수행
            List<BookDocument> books = esClient.searchBySorting(request);
            // ... (전체 개수 조회 로직 생략)
            return buildResponse(null, books, request);
        }

        // 2. RAG 기반 검색 (Semantic Search)
        if (query != null && !query.isEmpty()) {
            return performRagSearch(request);
        } 
        
        // 3. 키워드 기반 검색 (Keyword Search)
        // 키워드만 있는 경우, 가중치 기반 multi_match 검색 수행
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            List<BookDocument> books = esClient.searchByKeyword(request);
            // ... (전체 개수 조회 로직 생략)
            return buildResponse(null, books, request);
        }
        
        return buildResponse("검색 조건이 없습니다.", Collections.emptyList(), request);
    }

    // RAG 검색 파이프라인
    private BookSearchResponse performRagSearch(BookSearchRequest request) {
        String query = request.getQuery();
        
        // 1. Retrieval (Elasticsearch): 키워드+벡터 KNN 검색으로 후보군 50개 추출
        List<BookDocument> candidates = esClient.searchHybridCandidates(request);
        
        if (candidates.isEmpty()) {
            return buildResponse("검색 결과가 없습니다.", Collections.emptyList(), request);
        }

        // 2. Reranking (Augmentation): 재순위화
        List<String> texts = candidates.stream()
                .map(b -> String.format("제목: %s, 저자: %s, 설명: %s", 
                                        b.getTitle(), b.getAuthor(), b.getDescription()))
                .collect(Collectors.toList());
        
        // Reranker 호출 (상위 10개 인덱스와 점수 반환)
        List<RerankResult> rerankedResults = rerankerClient.rerank(query, texts);
        
        // 재순위화된 순서대로 최종 문서 K개 (예: 10개) 추출
        List<BookDocument> finalBooks = rerankedResults.stream()
                .map(result -> candidates.get(result.getIndex()))
                .collect(Collectors.toList());
        
        // 3. Generation (LLM): Gemini를 통한 검증 및 응답 생성
        String context = formatContextForLLM(query, finalBooks);
        String recommendation = geminiClient.generateResponse(context);

        // 4. 응답 구성
        return buildResponse(recommendation, finalBooks, request);
    }
    
    // LLM 컨텍스트 구성
    private String formatContextForLLM(String query, List<BookDocument> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자 질문: ").append(query).append("\n\n");
        sb.append("--- 다음 도서 정보를 참고하여 답변해 주세요 ---\n");
        
        for (BookDocument book : books) {
            sb.append(String.format("- [ID:%d] %s (저자: %s, 평점: %.1f/5)\n", 
                                    book.getBookId(), book.getTitle(), book.getAuthor(), book.getAverageRating()));
        }
        
        sb.append("\n지침: 사용자 질문에 가장 적합한 도서를 자연스러운 한국어로 추천해 주세요. 추천 이유를 반드시 포함해 주세요.");
        return sb.toString();
    }

    private BookSearchResponse buildResponse(String recommendation, List<BookDocument> books, BookSearchRequest request) {
         // 실제 구현에서는 totalHits를 ES 검색 결과에서 가져와야 함
        long total = books.isEmpty() ? 0 : 1000; 

        return BookSearchResponse.builder()
                .recommendationText(recommendation)
                .books(books)
                .totalHits(total)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .build();
    }
}