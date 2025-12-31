package com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundPublisherName extends EntityNotFoundException {
    public NotFoundPublisherName(String message) {
        super(message);
    }
}
