package com.daisobook.shop.booksearch.books_search.exception.custom.review;

import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;

public class NotFoundReview extends EntityNotFoundException {
    public NotFoundReview(String message) {
        super(message);
    }
}
