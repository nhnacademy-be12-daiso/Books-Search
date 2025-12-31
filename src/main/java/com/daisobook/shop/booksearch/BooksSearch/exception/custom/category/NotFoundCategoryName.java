package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundCategoryName extends EntityNotFoundException {
    public NotFoundCategoryName(String message) {
        super(message);
    }
}
