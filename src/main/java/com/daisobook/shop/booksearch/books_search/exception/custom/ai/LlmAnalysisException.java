package com.daisobook.shop.booksearch.books_search.exception.custom.ai;

import com.daisobook.shop.booksearch.books_search.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class LlmAnalysisException extends BusinessException {
    public LlmAnalysisException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
