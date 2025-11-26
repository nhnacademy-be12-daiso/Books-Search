package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class NotFoundReview extends RuntimeException {
    public NotFoundReview(String message) {
        super(message);
    }
}
