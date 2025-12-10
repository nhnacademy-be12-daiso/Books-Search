package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

public class NotFoundBookId extends RuntimeException {
    public NotFoundBookId(String message) {
        super(message);
    }
}
