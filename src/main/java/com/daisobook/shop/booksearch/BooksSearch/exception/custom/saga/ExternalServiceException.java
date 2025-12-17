package com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}
