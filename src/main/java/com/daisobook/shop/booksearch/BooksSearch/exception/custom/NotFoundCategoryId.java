package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundCategoryId extends RuntimeException {
    public NotFoundCategoryId(String message) {
        super(message);
    }
}
