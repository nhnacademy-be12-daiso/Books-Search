package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundCategoryId extends EntityNotFoundException {
    public NotFoundCategoryId(String message) {
        super(message);
    }
}
