package com.daisobook.shop.booksearch.books_search.exception.custom;

import org.springframework.http.HttpStatus;

// 409 Conflict 계열의 부모 (중복 데이터 등)
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}