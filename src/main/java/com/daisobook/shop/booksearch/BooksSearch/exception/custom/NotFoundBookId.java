package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundBookId extends RuntimeException {
    public NotFoundBookId(String message) {
        super(message);
    }
}
