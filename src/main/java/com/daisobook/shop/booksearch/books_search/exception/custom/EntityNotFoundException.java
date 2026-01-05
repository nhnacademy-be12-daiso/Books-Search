package com.daisobook.shop.booksearch.books_search.exception.custom;

import org.springframework.http.HttpStatus;

// 404 Not Found 계열의 부모
public class EntityNotFoundException extends RuntimeException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;
    public EntityNotFoundException(String message) {
        super(message);
    }
    public HttpStatus getHttpStatus() {
        return HTTP_STATUS;
    }
}