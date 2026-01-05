package com.daisobook.shop.booksearch.exception.custom.book;

import com.daisobook.shop.booksearch.exception.custom.EntityNotFoundException;

public class NotFoundBookId extends EntityNotFoundException {
    public NotFoundBookId(String message) {
        super(message);
    }
}
