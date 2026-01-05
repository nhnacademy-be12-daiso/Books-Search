package com.daisobook.shop.booksearch.exception.custom.review;

import com.daisobook.shop.booksearch.exception.custom.DuplicateResourceException;

public class DuplicatedReview extends DuplicateResourceException {
    public DuplicatedReview(String message) {
        super(message);
    }
}
