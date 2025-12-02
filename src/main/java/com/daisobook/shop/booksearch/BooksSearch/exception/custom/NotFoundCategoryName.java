package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundCategoryName extends RuntimeException {
    public NotFoundCategoryName(String message) {
        super(message);
    }
}
