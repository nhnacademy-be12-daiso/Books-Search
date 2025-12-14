package com.daisobook.shop.booksearch.BooksSearch.search.exception;

import com.daisobook.shop.booksearch.BooksSearch.search.exception.SearchModuleException;

public class RerankingException extends SearchModuleException {
    public RerankingException(String message, Throwable cause) { super("RerankingClient", message, cause); }
}