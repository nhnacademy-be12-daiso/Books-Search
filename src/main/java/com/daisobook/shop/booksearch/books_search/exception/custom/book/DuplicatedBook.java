package com.daisobook.shop.booksearch.books_search.exception.custom.book;

import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;

public class DuplicatedBook extends DuplicateResourceException {
    public DuplicatedBook(String message) {
        super(message);
    }
}
