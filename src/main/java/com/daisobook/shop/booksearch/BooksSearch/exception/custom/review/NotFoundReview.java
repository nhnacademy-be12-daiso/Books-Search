package com.daisobook.shop.booksearch.BooksSearch.exception.custom.review;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundReview extends EntityNotFoundException {
    public NotFoundReview(String message) {
        super(message);
    }
}
