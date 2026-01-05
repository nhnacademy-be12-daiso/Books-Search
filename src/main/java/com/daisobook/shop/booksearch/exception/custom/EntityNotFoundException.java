package com.daisobook.shop.booksearch.exception.custom;

import org.springframework.http.HttpStatus;

// 404 Not Found 계열의 부모
public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}