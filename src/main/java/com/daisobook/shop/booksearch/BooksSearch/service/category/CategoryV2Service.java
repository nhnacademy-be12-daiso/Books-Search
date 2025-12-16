package com.daisobook.shop.booksearch.BooksSearch.service.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.Map;

public interface CategoryV2Service {
    //bookCoreService에서 사용하는 메서드
    void assignCategoriesToBook(Book book, Long categoryId);
    void assignCategoriesToBooks(Map<String, Book> bookMap, Map<String, Long> categoryIdMap);
    void updateCategory(Book book, Long categoryId);
}
