package com.daisobook.shop.booksearch.books_search.exception.custom.category;

import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;

public class NotFoundCategoryId extends EntityNotFoundException {
    public NotFoundCategoryId(String message) {
        super(message);
    }
}
