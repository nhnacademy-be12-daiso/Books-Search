package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

public class NotFoundBookISBN extends RuntimeException {
    public NotFoundBookISBN(String message) {
        super(message);
    }
}
