package com.daisobook.shop.booksearch.books_search.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 400 Bad Request 계열의 부모 (변경 불가 등)
@Getter
public class InvalidRequestException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public InvalidRequestException(String message) {
        super(message);
    }
}