package com.daisobook.shop.booksearch.BooksSearch.search.exception;

public class LlmAnalysisException extends SearchModuleException {
    public LlmAnalysisException(String message, Throwable cause) { super("LlmClient", message, cause); }
}