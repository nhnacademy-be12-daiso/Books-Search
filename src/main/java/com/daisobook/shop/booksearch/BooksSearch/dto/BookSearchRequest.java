package com.daisobook.shop.booksearch.BooksSearch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookSearchRequest {
    // RAG 기반 자연어 검색 쿼리 (예: "아이들이 좋아할 만한 그림책 추천해줘")
    private String query; 
    
    // 키워드 검색용 필터 (예: "Java", "김영한")
    private String keyword; 
    
    // 정렬 기준 (예: "popularity", "latest", "price_asc", "rating")
    private String sortBy = "popularity"; 
    
    // 페이지네이션
    private int page = 1;
    private int size = 10;

    // 평점 필터링을 위한 최소 리뷰 수 (평점 정렬 시 사용)
    private Integer minReviewsForRating = 100; 
}