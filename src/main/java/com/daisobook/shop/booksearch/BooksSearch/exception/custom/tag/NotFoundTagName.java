package com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundTagName extends EntityNotFoundException {
    public NotFoundTagName(String message) {
        super(message);
    }
}
