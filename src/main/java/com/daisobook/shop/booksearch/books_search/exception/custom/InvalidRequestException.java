package com.daisobook.shop.booksearch.books_search.exception.custom;

import org.springframework.http.HttpStatus;

// 400 Bad Request 계열의 부모 (변경 불가 등)
public class InvalidRequestException extends RuntimeException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;
    public InvalidRequestException(String message) {
        super(message);
    }
    public HttpStatus getHttpStatus() {
        return HTTP_STATUS;
    }
}