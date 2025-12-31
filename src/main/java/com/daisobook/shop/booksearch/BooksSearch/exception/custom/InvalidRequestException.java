package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

import org.springframework.http.HttpStatus;

// 400 Bad Request 계열의 부모 (변경 불가 등)
public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}