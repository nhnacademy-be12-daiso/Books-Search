package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundBookCategory extends RuntimeException {
    public NotFoundBookCategory(String message) {
        super(message);
    }
}
