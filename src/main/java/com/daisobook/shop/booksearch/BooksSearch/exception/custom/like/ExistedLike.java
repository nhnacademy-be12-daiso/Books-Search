package com.daisobook.shop.booksearch.BooksSearch.exception.custom.like;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class ExistedLike extends DuplicateResourceException {
    public ExistedLike(String message) {
        super(message);
    }
}
