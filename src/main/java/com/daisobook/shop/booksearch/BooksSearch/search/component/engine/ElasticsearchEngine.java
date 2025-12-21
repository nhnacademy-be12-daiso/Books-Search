package com.daisobook.shop.booksearch.BooksSearch.search.component.engine;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

// Elasticsearch 검색 엔진 컴포넌트
@Component
@RequiredArgsConstructor
public class ElasticsearchEngine {

    private final BookRepository bookRepository;
    private static final int DEFAULT_FETCH_SIZE = 50;

    // 하이브리드 검색
    public List<Book> search(String query, List<Float> embedding) {
        List<Book> candidates = bookRepository.searchHybrid(query, embedding, DEFAULT_FETCH_SIZE);
        return candidates == null ? Collections.emptyList() : candidates;
    }

    public List<Book> searchByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}