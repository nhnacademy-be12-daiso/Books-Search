package com.daisobook.shop.booksearch.exception.custom.book;

import com.daisobook.shop.booksearch.exception.custom.EntityNotFoundException;

public class NotFoundBook extends EntityNotFoundException {
    public NotFoundBook(String message) {
        super(message);
    }
}
