package com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundPublisherId extends EntityNotFoundException {
    public NotFoundPublisherId(String message) {
        super(message);
    }
}
