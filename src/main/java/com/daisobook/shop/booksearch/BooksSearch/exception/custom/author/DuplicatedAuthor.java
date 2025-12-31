package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedAuthor extends DuplicateResourceException {
    public DuplicatedAuthor(String message) {
        super(message);
    }
}
