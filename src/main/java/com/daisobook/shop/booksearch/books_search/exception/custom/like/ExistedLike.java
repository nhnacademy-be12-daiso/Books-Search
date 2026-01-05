package com.daisobook.shop.booksearch.books_search.exception.custom.like;

import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;

public class ExistedLike extends DuplicateResourceException {
    public ExistedLike(String message) {
        super(message);
    }
}
