package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class DuplicatedAuthor extends RuntimeException {
    public DuplicatedAuthor(String message) {
        super(message);
    }
}
