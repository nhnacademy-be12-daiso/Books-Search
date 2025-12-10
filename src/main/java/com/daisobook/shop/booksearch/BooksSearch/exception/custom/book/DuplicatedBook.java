package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

public class DuplicatedBook extends RuntimeException {
    public DuplicatedBook(String message) {
        super(message);
    }
}
