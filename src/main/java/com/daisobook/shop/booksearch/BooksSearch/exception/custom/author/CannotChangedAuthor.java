package com.daisobook.shop.booksearch.BooksSearch.exception.custom.author;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedAuthor extends InvalidRequestException {
    public CannotChangedAuthor(String message) {
        super(message);
    }
}
