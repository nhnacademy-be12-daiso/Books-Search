package com.daisobook.shop.booksearch.books_search.exception.custom.image;

import com.daisobook.shop.booksearch.books_search.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class MinIOServiceException extends BusinessException {
    public MinIOServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
