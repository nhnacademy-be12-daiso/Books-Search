package com.daisobook.shop.booksearch.books_search.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 409 Conflict 계열의 부모 (중복 데이터 등)
@Getter
public class DuplicateResourceException extends RuntimeException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
    public DuplicateResourceException(String message) {
        super(message);
    }
    public HttpStatus getHttpStatus() {
        return HTTP_STATUS;
    }
}