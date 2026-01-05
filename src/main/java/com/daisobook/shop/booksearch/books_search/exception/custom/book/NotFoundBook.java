package com.daisobook.shop.booksearch.books_search.exception.custom.book;

import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;

public class NotFoundBook extends EntityNotFoundException {
    public NotFoundBook(String message) {
        super(message);
    }
}
