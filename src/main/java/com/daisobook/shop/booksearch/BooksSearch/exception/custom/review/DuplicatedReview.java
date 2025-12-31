package com.daisobook.shop.booksearch.BooksSearch.exception.custom.review;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedReview extends DuplicateResourceException {
    public DuplicatedReview(String message) {
        super(message);
    }
}
