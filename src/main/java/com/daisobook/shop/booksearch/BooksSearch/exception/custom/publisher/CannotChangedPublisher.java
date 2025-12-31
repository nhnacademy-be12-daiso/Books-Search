package com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedPublisher extends InvalidRequestException {
    public CannotChangedPublisher(String message) {
        super(message);
    }
}
