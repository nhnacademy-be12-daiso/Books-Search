package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookSearchRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookSearchResponse;
import com.daisobook.shop.booksearch.BooksSearch.service.impl.HybridSearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search/books")
public class BookSearchController {

    private final HybridSearchService searchService;

    public BookSearchController(HybridSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 하이브리드(RAG/키워드/정렬) 도서 검색 API
     * @param request 검색 조건 DTO (query, keyword, sortBy 등 포함)
     * @return 검색 결과 및 RAG 추천 텍스트
     */
    @GetMapping
    public BookSearchResponse search(BookSearchRequest request) {
        System.out.println("API 요청 수신: " + request);
        return searchService.searchBooks(request);
    }
}