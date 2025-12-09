package com.daisobook.shop.booksearch.BooksSearch.search.controller;

import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookJsonDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.management.BookManagementService;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController("BookSearchController")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final SearchService searchService;
    private final BookManagementService bookManagementService;

    // 데이터 임포트 실행 (관리자용)
    @PostMapping("/search/file-import")
    public ResponseEntity<String> importBooks(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }

        // 파일 객체를 서비스로 넘김
        String result = bookManagementService.importBooks(String.valueOf(file));
        return ResponseEntity.ok(result);
    }

    // 책 한 권 정보를 받아서 수정/등록하는 API
    @PutMapping("/search/update")
    public ResponseEntity<String> updateBook(@Valid @RequestBody BookJsonDto bookDto) {
        // 서비스가 "수정되었습니다" 또는 "생성되었습니다" 메시지를 줌
        String resultMessage = bookManagementService.upsertBook(bookDto);
        return ResponseEntity.ok(resultMessage);
    }

    // 기본 도서 검색
    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> search(@RequestParam String query) {
        SearchResponseDto result = searchService.basicSearch(query);
        return ResponseEntity.ok(result);
    }

    // AI 도서 검색
    @GetMapping("/ai-search")
    public ResponseEntity<SearchResponseDto> aiSearch(@RequestParam String query) {
        SearchResponseDto result = searchService.aiSearch(query);
        return ResponseEntity.ok(result);
    }
}
