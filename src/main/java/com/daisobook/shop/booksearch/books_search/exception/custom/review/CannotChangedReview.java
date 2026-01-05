package com.daisobook.shop.booksearch.books_search.exception.custom.review;

import com.daisobook.shop.booksearch.books_search.exception.custom.InvalidRequestException;

public class CannotChangedReview extends InvalidRequestException {
    public CannotChangedReview(String message) {
        super(message);
    }
}
