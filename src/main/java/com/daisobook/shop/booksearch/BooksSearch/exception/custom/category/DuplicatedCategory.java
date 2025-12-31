package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedCategory extends DuplicateResourceException {
    public DuplicatedCategory(String message) {
        super(message);
    }
}
