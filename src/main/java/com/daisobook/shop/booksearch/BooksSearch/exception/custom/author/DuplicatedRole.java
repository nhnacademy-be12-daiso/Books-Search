package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedRole extends DuplicateResourceException {
    public DuplicatedRole(String message) {
        super(message);
    }
}
