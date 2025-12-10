package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

public class NotFoundBook extends RuntimeException {
    public NotFoundBook(String message) {
        super(message);
    }
}
