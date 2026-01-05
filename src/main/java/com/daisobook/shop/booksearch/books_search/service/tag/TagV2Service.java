package com.daisobook.shop.booksearch.books_search.service.tag;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;

import java.util.List;
import java.util.Map;

public interface TagV2Service {
    // bookCoreService에서 사용하는 메서드
    void assignTagsToBook(Book book, List<String> tagNameList);
    void assignTagsToBooks(Map<String, Book> bookMap, Map<String, List<String>> tagNameListMap);
    void updateTagOfBook(Book book, List<String> tagNameList);
    void deleteTagOfBook(Book book);
}
