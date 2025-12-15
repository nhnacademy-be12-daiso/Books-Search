package com.daisobook.shop.booksearch.BooksSearch.search.controller;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.BookManagementService;
import com.daisobook.shop.booksearch.BooksSearch.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController("BookSearchController")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookManagementService bookManagementService;
    private final SearchService searchService;

    // 책 한 권 정보를 받아서 수정/등록하는 API
    @PutMapping("/search/update")
    public ResponseEntity<String> updateBook(@Valid @RequestBody Book bookDto) {
        BookManagementService.OperationResult result = bookManagementService.upsertBook(bookDto);
        if (result.success()) {
            return ResponseEntity.ok(result.message());
        }
        // ❗배포 환경에서 예외로 끊지 않고, 실패는 502(Bad Gateway)로 내려서 호출자가 판단하게 한다.
        return ResponseEntity.status(502).body(result.message());
    }

    @DeleteMapping("/search/delete/{isbn}")
    public ResponseEntity<String> deleteBook(@PathVariable String isbn) {
        BookManagementService.OperationResult result = bookManagementService.deleteBook(isbn);
        if (result.success()) {
            return ResponseEntity.ok(result.message());
        }
        return ResponseEntity.status(502).body(result.message());
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
