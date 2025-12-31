package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class ExistedCategory extends DuplicateResourceException {
    public ExistedCategory(String message) {
        super(message);
    }
}
