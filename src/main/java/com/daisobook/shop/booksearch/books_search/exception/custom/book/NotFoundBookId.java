package com.daisobook.shop.booksearch.books_search.exception.custom.book;

import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;

public class NotFoundBookId extends EntityNotFoundException {
    public NotFoundBookId(String message) {
        super(message);
    }
}
