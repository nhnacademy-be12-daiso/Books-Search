package com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedPublisher extends DuplicateResourceException {
    public DuplicatedPublisher(String message) {
        super(message);
    }
}
