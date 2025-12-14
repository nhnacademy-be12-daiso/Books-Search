package com.daisobook.shop.booksearch.BooksSearch.search.exception;

import com.daisobook.shop.booksearch.BooksSearch.search.exception.SearchModuleException;

public class ElasticsearchException extends SearchModuleException {
    public ElasticsearchException(String message, Throwable cause) { super("ElasticsearchEngine", message, cause); }
}