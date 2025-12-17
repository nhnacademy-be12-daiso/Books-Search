package com.daisobook.shop.booksearch.BooksSearch.exception;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({FailedSerializationException.class, ExternalServiceException.class})
    public ResponseEntity<String> handleExternalServiceException(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }
}
