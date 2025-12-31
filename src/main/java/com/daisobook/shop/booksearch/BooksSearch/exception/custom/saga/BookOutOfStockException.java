package com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class BookOutOfStockException extends BusinessException {
    public BookOutOfStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
