package com.daisobook.shop.booksearch.exception.custom.saga;

import com.daisobook.shop.booksearch.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class FailedSerializationException extends BusinessException {
    public FailedSerializationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
