package com.daisobook.shop.booksearch.books_search.exception.custom.book;

import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;

public class NotFoundBookISBN extends EntityNotFoundException {
    public NotFoundBookISBN(String message) {
        super(message);
    }
}
