package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class CannotChangedCategory extends InvalidRequestException {
    public CannotChangedCategory(String message) {
        super(message);
    }
}
