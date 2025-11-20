package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundPublisherId extends RuntimeException {
    public NotFoundPublisherId(String message) {
        super(message);
    }
}
