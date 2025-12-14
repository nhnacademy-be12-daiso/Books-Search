package com.daisobook.shop.booksearch.BooksSearch.search.exception;

import com.daisobook.shop.booksearch.BooksSearch.search.exception.SearchModuleException;

public class LlmAnalysisException extends SearchModuleException {
    public LlmAnalysisException(String message, Throwable cause) { super("LlmClient", message, cause); }
}