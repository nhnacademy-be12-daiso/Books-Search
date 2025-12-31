package com.daisobook.shop.booksearch.BooksSearch.exception.custom.ai;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class GeminiQuotaException extends BusinessException {
    public GeminiQuotaException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
