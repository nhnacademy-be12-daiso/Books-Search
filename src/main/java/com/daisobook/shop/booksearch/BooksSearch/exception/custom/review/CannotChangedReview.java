package com.daisobook.shop.booksearch.BooksSearch.exception.custom.review;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedReview extends InvalidRequestException {
    public CannotChangedReview(String message) {
        super(message);
    }
}
