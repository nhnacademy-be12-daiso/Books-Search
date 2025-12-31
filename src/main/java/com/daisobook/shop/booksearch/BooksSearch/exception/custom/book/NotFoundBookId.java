package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundBookId extends EntityNotFoundException {
    public NotFoundBookId(String message) {
        super(message);
    }
}
