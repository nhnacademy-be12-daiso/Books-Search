package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundAuthor extends RuntimeException {
    public NotFoundAuthor(String message) {
        super(message);
    }
}
