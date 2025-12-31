package com.daisobook.shop.booksearch.BooksSearch.exception.custom.mapper;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class FailObjectMapper extends BusinessException {
    public FailObjectMapper(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
