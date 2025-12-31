package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundBookISBN extends EntityNotFoundException {
    public NotFoundBookISBN(String message) {
        super(message);
    }
}
