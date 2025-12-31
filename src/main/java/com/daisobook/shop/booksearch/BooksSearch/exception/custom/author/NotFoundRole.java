package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundRole extends EntityNotFoundException {
    public NotFoundRole(String message) {
        super(message);
    }
}
