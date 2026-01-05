package com.daisobook.shop.booksearch.books_search.exception.custom.category;

import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;

public class DuplicatedCategory extends DuplicateResourceException {
    public DuplicatedCategory(String message) {
        super(message);
    }
}
