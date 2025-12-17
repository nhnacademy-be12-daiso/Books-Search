package com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga;

public class FailedSerializationException extends RuntimeException {
    public FailedSerializationException(String message) {
        super(message);
    }
}
