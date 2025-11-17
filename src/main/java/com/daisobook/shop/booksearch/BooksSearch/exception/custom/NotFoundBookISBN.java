package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundBookISBN extends RuntimeException {
    public NotFoundBookISBN(String message) {
        super(message);
    }
}
