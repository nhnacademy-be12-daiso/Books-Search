package com.daisobook.shop.booksearch.exception.custom.review;

import com.daisobook.shop.booksearch.exception.custom.EntityNotFoundException;

public class NotFoundReview extends EntityNotFoundException {
    public NotFoundReview(String message) {
        super(message);
    }
}
