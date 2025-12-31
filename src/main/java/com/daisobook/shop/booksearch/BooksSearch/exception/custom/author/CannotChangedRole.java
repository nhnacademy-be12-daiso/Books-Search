package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedRole extends InvalidRequestException {
    public CannotChangedRole(String message) {
        super(message);
    }
}
