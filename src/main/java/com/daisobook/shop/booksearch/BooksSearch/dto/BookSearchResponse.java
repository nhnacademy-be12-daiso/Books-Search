package com.daisobook.shop.booksearch.BooksSearch.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class BookSearchResponse {
    // RAG/LLM이 생성한 자연어 추천 응답
    private String recommendationText; 
    
    // 검색 결과 리스트 (Elasticsearch 또는 RAG 최종 결과)
    private List<BookDocument> books;
    
    // 전체 검색 건수
    private long totalHits; 
    
    // 페이지네이션 정보
    private int currentPage;
    private int pageSize;
}