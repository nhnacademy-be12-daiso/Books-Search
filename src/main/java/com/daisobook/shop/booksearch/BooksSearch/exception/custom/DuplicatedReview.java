package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class DuplicatedReview extends RuntimeException {
    public DuplicatedReview(String message) {
        super(message);
    }
}
