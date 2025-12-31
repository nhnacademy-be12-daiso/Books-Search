package com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundTagId extends EntityNotFoundException {
    public NotFoundTagId(String message) {
        super(message);
    }
}
