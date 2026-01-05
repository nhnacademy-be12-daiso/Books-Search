package com.daisobook.shop.booksearch.books_search.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 404 Not Found 계열의 부모
@Getter
public class EntityNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    public EntityNotFoundException(String message) {
        super(message);
    }
}