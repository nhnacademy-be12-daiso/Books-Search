package com.daisobook.shop.booksearch.BooksSearch.search.exception;

import com.daisobook.shop.booksearch.BooksSearch.search.exception.SearchModuleException;

public class EmbeddingException extends SearchModuleException {
    public EmbeddingException(String message, Throwable cause) { super("EmbeddingClient", message, cause); }
}