package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedBook extends DuplicateResourceException {
    public DuplicatedBook(String message) {
        super(message);
    }
}
