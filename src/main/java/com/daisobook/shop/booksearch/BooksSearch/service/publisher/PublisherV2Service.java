package com.daisobook.shop.booksearch.BooksSearch.service.publisher;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.Map;

public interface PublisherV2Service {
    //bookCoreService 에서 사용하는 메서드
    void assignPublisherToBook(Book book, String publisherName);
    void assignPublisherToBooks(Map<String, Book> bookMap, Map<String, String> publisherNameMap);
    void updatePublisherOfBook(Book book, String publisherName);
    void deletePublisherOfBook(Book book);
}
