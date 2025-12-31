package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundAuthor extends EntityNotFoundException {
    public NotFoundAuthor(String message) {
        super(message);
    }
}
