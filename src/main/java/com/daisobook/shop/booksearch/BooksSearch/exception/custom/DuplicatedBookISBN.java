package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class DuplicatedBookISBN extends RuntimeException {
    public DuplicatedBookISBN(String message) {
        super(message);
    }
}
