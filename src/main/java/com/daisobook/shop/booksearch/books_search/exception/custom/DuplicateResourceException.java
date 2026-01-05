package com.daisobook.shop.booksearch.books_search.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 409 Conflict 계열의 부모 (중복 데이터 등)
@Getter
public class DuplicateResourceException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.CONFLICT;
    public DuplicateResourceException(String message) {
        super(message);
    }
}