package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundBook extends EntityNotFoundException {
    public NotFoundBook(String message) {
        super(message);
    }
}
