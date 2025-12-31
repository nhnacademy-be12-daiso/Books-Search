package com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;

public class DuplicatedTag extends DuplicateResourceException {
    public DuplicatedTag(String message) {
        super(message);
    }
}
