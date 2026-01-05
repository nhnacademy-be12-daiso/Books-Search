package com.daisobook.shop.booksearch.books_search.exception.custom.review;

import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;

public class DuplicatedReview extends DuplicateResourceException {
    public DuplicatedReview(String message) {
        super(message);
    }
}
