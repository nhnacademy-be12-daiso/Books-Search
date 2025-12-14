package com.daisobook.shop.booksearch.BooksSearch.search.controller;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.BookManagementService;
import com.daisobook.shop.booksearch.BooksSearch.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController("BookSearchController")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookManagementService bookManagementService;
    private final SearchService searchService;

    // 책 한 권 정보를 받아서 수정/등록하는 API
    @PutMapping("/search/update")
    public ResponseEntity<String> updateBook(@Valid @RequestBody Book bookDto) {
        // 서비스가 "수정되었습니다" 또는 "생성되었습니다" 메시지를 줌
        bookManagementService.upsertBook(bookDto);
        return ResponseEntity.ok("도서 정보가 성공적으로 저장되었습니다.");
    }

    // 기본 도서 검색
    @GetMapping("/search")
    public SearchResponseDto search(@RequestParam String query) {
        return searchService.basicSearch(query);
    }

    // AI 도서 검색
    @GetMapping("/ai-search")
    public SearchResponseDto aiSearch(@RequestParam String query) {
        return searchService.aiSearch(query);
    }
}
