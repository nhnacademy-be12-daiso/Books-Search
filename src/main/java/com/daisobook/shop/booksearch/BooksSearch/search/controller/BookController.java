package com.daisobook.shop.booksearch.BooksSearch.search.controller;

import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("BookSearchController")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final SearchService searchService;

    // 기본 도서 검색 (통합 검색)
    @GetMapping("/search")
    public SearchResponseDto search(@RequestParam String query) {
        return searchService.search(query);
    }

    // (호환) /ai-search 도 동일 동작
    @GetMapping("/ai-search")
    public SearchResponseDto aiSearch(@RequestParam String query) {
        return searchService.search(query);
    }

}
