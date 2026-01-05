package com.daisobook.shop.booksearch.books_search.exception.custom.category;

import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;

public class ExistedCategory extends DuplicateResourceException {
    public ExistedCategory(String message) {
        super(message);
    }
}
