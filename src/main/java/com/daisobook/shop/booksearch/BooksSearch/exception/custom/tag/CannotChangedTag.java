package com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedTag extends InvalidRequestException {
    public CannotChangedTag(String message) {
        super(message);
    }
}
