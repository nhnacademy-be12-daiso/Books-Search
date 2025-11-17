package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundBook extends RuntimeException {
    public NotFoundBook(String message) {
        super(message);
    }
}
