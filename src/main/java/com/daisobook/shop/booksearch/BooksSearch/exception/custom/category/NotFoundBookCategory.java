package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

public class NotFoundBookCategory extends RuntimeException {
    public NotFoundBookCategory(String message) {
        super(message);
    }
}
